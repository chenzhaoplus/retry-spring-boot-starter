package com.smcaiot.retry.starter.app;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.smcaiot.retry.starter.annotation.RetryField;
import com.smcaiot.retry.starter.annotation.RetryParam;
import com.smcaiot.retry.starter.constants.RetryStatus;
import com.smcaiot.retry.starter.entity.RetryQueue;
import com.smcaiot.retry.starter.service.RetryQueueService;
import com.smcaiot.retry.starter.util.ReflectUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author: cz
 * @Date: 2023/7/13
 * @Description:
 */
@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Autowired
    private RetryQueueService retryQueueService;

    @Pointcut("@annotation(com.smcaiot.retry.starter.annotation.RetryMethod)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object aroud(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        if (ArrayUtil.isEmpty(args)) {
            return true;
        }
        RetryQuery retryQuery = getRetryQuery(args);
        if (Objects.isNull(retryQuery)) {
            return true;
        }
        beforeRetry(retryQuery);
        boolean result = doRetry(point, retryQuery);
        //afterRetry(retryQuery);
        return result;
    }

    private RetryQuery getRetryQuery(Object[] args) {
        for (Object arg : args) {
            if (Objects.isNull(arg)) {
                continue;
            }
            Class<?> clazz = arg.getClass();
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                if (!clazz.isAnnotationPresent(RetryParam.class)) {
                    log.warn("重试时，请标注重试字段");
                    return null;
                }
                Field[] fields = clazz.getDeclaredFields();
                if (ArrayUtil.isEmpty(fields)) {
                    log.warn("重试时，请标注重试字段");
                    return null;
                }
                Optional<Field> idField = Arrays.stream(fields).filter(this::filterRetryId).findFirst();
                Object retryId = ReflectUtil.getFieldValue(arg, idField.get());
                if (Objects.isNull(retryId)) {
                    log.warn("重试时，请标注重试ID");
                    return null;
                }
                Optional<Field> typeField = Arrays.stream(fields).filter(this::filterRetryType).findFirst();
                Object retryType = ReflectUtil.getFieldValue(arg, typeField.get());
                if (Objects.isNull(retryType)) {
                    log.warn("重试时，请标注重试类型");
                    return null;
                }
                Optional<Field> doCallbackField = Arrays.stream(fields).filter(this::filterDoCallback).findFirst();
                Object doCallback = ReflectUtil.getFieldValue(arg, doCallbackField.get());
                return new RetryQuery().setArg(arg).setRetryId(String.valueOf(retryId))
                        .setRetryType(String.valueOf(retryType))
                        .setDoCallback(Objects.isNull(doCallback) ? false : (Boolean) doCallback);
            }
        }
        log.warn("重试时，请标注重试类型和重试ID");
        return null;
    }

    private boolean filterRetryId(Field f) {
        return Objects.nonNull(f)
                && f.isAnnotationPresent(RetryField.class) && f.getAnnotation(RetryField.class).retryId();
    }

    private boolean filterRetryType(Field f) {
        return Objects.nonNull(f)
                && f.isAnnotationPresent(RetryField.class) && f.getAnnotation(RetryField.class).retryType();
    }

    private boolean filterDoCallback(Field f) {
        return Objects.nonNull(f)
                && f.isAnnotationPresent(RetryField.class) && f.getAnnotation(RetryField.class).doCallback();
    }

    private void beforeRetry(RetryQuery retryQuery) {
        RetryQueue queue = retryQueueService.findByRetryId(retryQuery.getRetryId(), retryQuery.getRetryType());
        retryQuery.setQueue(queue)
                .setGoOn(Objects.isNull(retryQuery.getQueue()) ||
                        RetryStatus.to_be_retry.getCode().equals(queue.getRetryStatus()));
    }

    private boolean doRetry(ProceedingJoinPoint point, RetryQuery retryQuery) throws Throwable {
        if (!retryQuery.getGoOn()) {
            return true;
        }
        try {
            return retryQueueService.doRetry(point, retryQuery);
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
            retryQuery.getQueue().setRetryStatus(RetryStatus.to_be_retry.getCode());
            retryQueueService.saveOrUpdateByRetryId4Retry(retryQuery.getQueue());// 次数+1
            return false;
        }
    }

    private RetryQueue newRetryQueue(RetryQuery retryQuery) {
        RetryQueue queue = new RetryQueue().setRetryId(retryQuery.getRetryId())
                .setRetryType(retryQuery.getRetryType())
                //.setRetryTimes()
                //.setNextTime()
                .setRetryParams(JSON.toJSONString(retryQuery.getArg()))
                .setLastTime(new Date());
        return queue;
    }

    //private void afterRetry(RetryQuery retryQuery) {
    //    if (!retryQuery.getGoOn()) {
    //        return;
    //    }
    //    retryQuery.getQueue().setRetryTimes(0).setNextTime(DateUtil.offsetMinute(new Date(), 1))
    //            .setRetryStatus(getRetryStatus(retryQuery.getDoCallback()));
    //    retryQuery.setGoOn(retryQueueService.updateById(retryQuery.getQueue()));
    //}

    @Data
    @Accessors(chain = true)
    public static class RetryQuery {
        private Object arg;
        private String retryId;
        private String retryType;
        private RetryQueue queue;
        private Boolean goOn = true;
        private Boolean doCallback;
    }

}
