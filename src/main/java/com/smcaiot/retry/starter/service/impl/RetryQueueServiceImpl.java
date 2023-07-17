package com.smcaiot.retry.starter.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smcaiot.retry.starter.app.RetryAspect;
import com.smcaiot.retry.starter.app.RetryProperties;
import com.smcaiot.retry.starter.constants.RetryStatus;
import com.smcaiot.retry.starter.entity.*;
import com.smcaiot.retry.starter.mapper.RetryQueueMapper;
import com.smcaiot.retry.starter.service.RetryCallback;
import com.smcaiot.retry.starter.service.RetryQueueService;
import com.smcaiot.retry.starter.util.SpringContext;
import com.smcaiot.retry.starter.util.PageParam;
import com.smcaiot.retry.starter.util.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
@Service
@Slf4j
public class RetryQueueServiceImpl extends ServiceImpl<RetryQueueMapper, RetryQueue>
        implements RetryQueueService {

    @Autowired
    private RetryProperties retryProp;
    @Autowired
    private RetryCallback retryCallback;

    @Override
    public void retry() {
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(Optional.ofNullable(retryProp.getRetryPageSize()).orElse(20));
        for (; ; ) {
            PageResult<RetryQueue> pageResult = findPage2BeRetried(pageParam);
            List<RetryQueue> list = pageResult.getContent();
            log.info("重试任务分页查询，list size: {}", CollUtil.isEmpty(list) ? 0 : list.size());
            if (CollUtil.isEmpty(list)) {
                break;
            }
            list.stream().forEach(queue -> {
                Class<?> clz = getClass(queue.getRetryClass());
                invokeMethod(clz, queue.getRetryMethod(), JSON.parse(queue.getRetryParams()));
            });
            pageParam.setPageNum(pageParam.getPageNum() + 1);
        }
    }

    @Override
    public void callback() {
        PageParam pageParam = new PageParam();
        pageParam.setPageSize(Optional.ofNullable(retryProp.getCallbackPageSize()).orElse(50));
        for (; ; ) {
            PageResult<RetryQueue> pageResult = findPage2BeCallback(pageParam);
            List<RetryQueue> list = pageResult.getContent();
            log.info("回调任务分页查询，list size: {}", CollUtil.isEmpty(list) ? 0 : list.size());
            if (CollUtil.isEmpty(list)) {
                break;
            }
            try {
                SpringContext.getBean(getClass()).callbackAndFinish(list);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                list.forEach(queue -> {
                    queue.setRetryTimes(queue.getRetryTimes() + 1);
                    queue.setNextTime(offsetMinute(queue.getRetryTimes() + 1));
                });
                updateBatchById(list);
            }
            pageParam.setPageNum(pageParam.getPageNum() + 1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void callbackAndFinish(List<RetryQueue> list) {
        list.forEach(queue -> {
            queue.setRetryStatus(RetryStatus.finished.getCode());
            queue.setRetryTimes(queue.getRetryTimes() + 1);
            queue.setNextTime(offsetMinute(queue.getRetryTimes() + 1));
        });
        updateBatchById(list);
        Assert.isTrue(retryCallback.doCallback(list), "重试回调失败");
    }

    private Date offsetMinute(int minutes) {
        return DateUtil.offsetMinute(new Date(), minutes);
    }

    private <T> T invokeMethod(Class<?> clz, String methodName, Object... args) {
        if (Objects.isNull(clz) || StrUtil.isBlank(methodName)) {
            return null;
        }
        Object bean = SpringContext.getBean(clz);
        Method method = ReflectUtil.getMethodByName(bean.getClass(), methodName);
        if (method == null) {
            log.warn("未找到指定方法：{}", bean.getClass().getName() + "." + methodName);
            return null;
        }
        return ReflectUtil.invoke(bean, method, args);
    }

    private Class<?> getClass(String clzName) {
        try {
            return Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            log.warn("未找到指定类：{}", clzName);
            return null;
        }
    }

    @Override
    public RetryQueue find2BeRetried(String retryId, String retryType) {
        List<RetryQueue> list = getBaseMapper().find2BeRetried(retryId, retryType);
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public PageResult<RetryQueue> findPage2BeRetried(PageParam pageParam) {
        try (Page<RetryQueue> page = PageHelper.startPage(pageParam)) {
            List<RetryQueue> list = getBaseMapper().findPage2BeRetried(pageParam);
            PageResult<RetryQueue> pageResult = new PageResult<>();
            pageResult.setContent(list);
            pageResult.setTotalElements(page.getTotal());
            return pageResult;
        }
    }

    @Override
    public RetryQueue findByRetryId(String retryId, String retryType) {
        List<RetryQueue> list = getBaseMapper().findByRetryId(retryId, retryType);
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public PageResult<RetryQueue> findPage2BeCallback(PageParam pageParam) {
        try (Page<RetryQueue> page = PageHelper.startPage(pageParam)) {
            List<RetryQueue> list = getBaseMapper().findPage2BeCallback(pageParam);
            PageResult<RetryQueue> pageResult = new PageResult<>();
            pageResult.setContent(list);
            pageResult.setTotalElements(page.getTotal());
            return pageResult;
        }
    }

    @Override
    public boolean updateByRetryId4Retry(RetryQueue queue) {
        int cnt = getBaseMapper().updateByRetryId4Retry(queue);
        return SqlHelper.retBool(cnt);
    }

    @Override
    public boolean updateByRetryId4Callback(RetryQueue queue) {
        int cnt = getBaseMapper().updateByRetryId4Callback(queue);
        return SqlHelper.retBool(cnt);
    }

    @Override
    public boolean saveOrUpdateByRetryId4Retry(RetryQueue queue) {
        boolean update = updateByRetryId4Retry(queue);
        if (update) {
            return true;
        }
        return insertByRetryId4Retry(queue);
    }

    @Override
    public boolean insertByRetryId4Retry(RetryQueue queue) {
        int cnt = getBaseMapper().insertByRetryId4Retry(queue);
        return SqlHelper.retBool(cnt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doRetry(ProceedingJoinPoint point, RetryAspect.RetryQuery retryQuery) throws Throwable {
        if (Objects.isNull(retryQuery.getQueue())) {
            RetryQueue queue = newRetryQueue(retryQuery).setRetryStatus(getRetryStatus(retryQuery.getDoCallback()))
                    .setRetryClass(RetryAspect.getMethodClass(point))
                    .setRetryMethod(RetryAspect.getMethodName(point));
            retryQuery.setGoOn(insertByRetryId4Retry(queue)).setQueue(queue);
        } else {
            retryQuery.getQueue().setRetryStatus(getRetryStatus(retryQuery.getDoCallback()));
            retryQuery.setGoOn(updateByRetryId4Retry(retryQuery.getQueue()));
        }
        if (!retryQuery.getGoOn()) {
            return true;
        }
        Assert.isTrue((Boolean) point.proceed(), "重试失败");
        return true;
    }

    private String getRetryStatus(boolean doCallback){
        return doCallback ? RetryStatus.to_be_callback.getCode() : RetryStatus.finished.getCode();
    }

    private RetryQueue newRetryQueue(RetryAspect.RetryQuery retryQuery) {
        RetryQueue queue = new RetryQueue().setRetryId(retryQuery.getRetryId())
                .setRetryType(retryQuery.getRetryType())
                //.setRetryTimes()
                //.setNextTime()
                .setRetryParams(JSON.toJSONString(retryQuery.getArg()))
                .setLastTime(new Date());
        return queue;
    }

}




