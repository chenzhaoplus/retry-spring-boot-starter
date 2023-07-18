package com.smcaiot.retry.starter.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.smcaiot.retry.starter.app.RetryAspect;
import com.smcaiot.retry.starter.app.RetryProperties;
import com.smcaiot.retry.starter.constants.RetryStatus;
import com.smcaiot.retry.starter.entity.FindPage2BeCallbackParam;
import com.smcaiot.retry.starter.entity.FindPage2BeRetriedParam;
import com.smcaiot.retry.starter.entity.RetryQueue;
import com.smcaiot.retry.starter.mapper.RetryQueueMapper;
import com.smcaiot.retry.starter.service.RetryCallback;
import com.smcaiot.retry.starter.service.RetryQueueService;
import com.smcaiot.retry.starter.util.PageParam;
import com.smcaiot.retry.starter.util.PageResult;
import com.smcaiot.retry.starter.util.ReflectUtils;
import com.smcaiot.retry.starter.util.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        FindPage2BeRetriedParam pageParam = new FindPage2BeRetriedParam();
        pageParam.setPageSize(Optional.ofNullable(retryProp.getRetryPageSize()).orElse(20));
        pageParam.setStopRetryTypes(retryProp.getStopRetryTypes());
        pageParam.setMaxRetryTimes(retryProp.getMaxRetryTimes());
        for (; ; ) {
            PageResult<RetryQueue> pageResult = findPage2BeRetried(pageParam);
            List<RetryQueue> list = pageResult.getContent();
            log.info("重试任务分页查询，list size: {}", CollUtil.isEmpty(list) ? 0 : list.size());
            if (CollUtil.isEmpty(list)) {
                break;
            }
            list.stream().forEach(queue -> {
                Class<?> clz = ReflectUtils.getClass(queue.getRetryClass());
                ReflectUtils.invokeMethod(clz, queue.getRetryMethod(), JSON.parse(queue.getRetryParams()));
            });
            pageParam.setPageNum(pageParam.getPageNum() + 1);
        }
    }

    @Override
    public void callback() {
        FindPage2BeCallbackParam pageParam = new FindPage2BeCallbackParam();
        pageParam.setPageSize(Optional.ofNullable(retryProp.getCallbackPageSize()).orElse(50));
        pageParam.setStopCallbackTypes(retryProp.getStopCallbackTypes());
        pageParam.setMaxRetryTimes(retryProp.getMaxRetryTimes());
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
                list.forEach(queue -> queue.setRetryStatus(RetryStatus.to_be_callback.getCode()));
                updateBatchById(list);
            }
            pageParam.setPageNum(pageParam.getPageNum() + 1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void callbackAndFinish(List<RetryQueue> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Date now = new Date();
        list.forEach(queue -> {
            queue.setRetryStatus(RetryStatus.finished.getCode());
            queue.setRetryTimes(queue.getRetryTimes() + 1).setLastTime(now);
            queue.setNextTime(offsetMinute(queue.getRetryTimes() + 1));
        });
        updateBatchById(list);
        List<String> retryIds = list.stream().map(RetryQueue::getRetryId).collect(Collectors.toList());
        Assert.isTrue(retryCallback.doCallback(list), "重试回调失败: {}", JSON.toJSONString(retryIds));
        //Assert.isTrue(false, "重试回调失败: {}", JSON.toJSONString(retryIds));// TODO
        log.debug("重试回调成功: {}", JSON.toJSONString(list));
    }

    private Date offsetMinute(int minutes) {
        return DateUtil.offsetMinute(new Date(), minutes);
    }

    @Override
    public RetryQueue find2BeRetried(String retryId, String retryType) {
        List<RetryQueue> list = getBaseMapper().find2BeRetried(retryId, retryType);
        return CollUtil.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public PageResult<RetryQueue> findPage2BeRetried(FindPage2BeRetriedParam param) {
        try (Page<RetryQueue> page = PageHelper.startPage(param)) {
            List<RetryQueue> list = getBaseMapper().findPage2BeRetried(param);
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
    public PageResult<RetryQueue> findPage2BeCallback(FindPage2BeCallbackParam param) {
        try (Page<RetryQueue> page = PageHelper.startPage(param)) {
            List<RetryQueue> list = getBaseMapper().findPage2BeCallback(param);
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
                    .setRetryClass(ReflectUtils.getMethodClass(point))
                    .setRetryMethod(ReflectUtils.getMethodName(point));
            retryQuery.setGoOn(insertByRetryId4Retry(queue)).setQueue(queue);
        } else {
            if (reachNextTime(retryQuery.getQueue().getNextTime())) {
                retryQuery.getQueue().setRetryStatus(getRetryStatus(retryQuery.getDoCallback()));
                retryQuery.setGoOn(updateByRetryId4Callback(retryQuery.getQueue()));
            } else {
                retryQuery.setGoOn(false);
            }
        }
        if (!retryQuery.getGoOn()) {
            return true;
        }
        Assert.isTrue((Boolean) point.proceed(), "重试失败, retryId：{}", retryQuery.getRetryId());
        //Assert.isTrue(false, "重试失败, retryId：{}", retryQuery.getRetryId());// TODO
        log.debug("重试成功, retryId: {}", retryQuery.getRetryId());
        return true;
    }

    private boolean reachNextTime(Date nextTime) {
        return DateUtil.compare(nextTime, new Date()) <= 0;
    }

    private String getRetryStatus(boolean doCallback) {
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




