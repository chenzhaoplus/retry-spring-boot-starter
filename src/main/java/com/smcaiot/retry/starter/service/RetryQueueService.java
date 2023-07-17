package com.smcaiot.retry.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smcaiot.retry.starter.app.RetryAspect;
import com.smcaiot.retry.starter.entity.RetryQueue;
import com.smcaiot.retry.starter.util.PageParam;
import com.smcaiot.retry.starter.util.PageResult;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 *
 */
public interface RetryQueueService extends IService<RetryQueue> {

    //void retryMsg();

    //void retryVoice();

    void retry();

    void callback();

    RetryQueue find2BeRetried(String retryId, String retryType);

    PageResult<RetryQueue> findPage2BeRetried(PageParam pageParam);

    RetryQueue findByRetryId(String retryId, String retryType);

    PageResult<RetryQueue> findPage2BeCallback(PageParam pageParam);

    boolean updateByRetryId4Retry(RetryQueue queue);

    boolean updateByRetryId4Callback(RetryQueue queue);

    boolean saveOrUpdateByRetryId4Retry(RetryQueue queue);

    boolean insertByRetryId4Retry(RetryQueue queue);

    boolean doRetry(ProceedingJoinPoint point, RetryAspect.RetryQuery retryQuery) throws Throwable;

}
