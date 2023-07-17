package com.smcaiot.retry.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smcaiot.retry.starter.entity.RetryQueue;
import com.smcaiot.retry.starter.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.smcaiot.ibmp.police.entity.RetryQueue
 */
public interface RetryQueueMapper extends BaseMapper<RetryQueue> {

    List<RetryQueue> find2BeRetried(@Param("retryId") String retryId, @Param("retryType") String retryType);

    List<RetryQueue> findPage2BeRetried(@Param("pageParam") PageParam pageParam);

    List<RetryQueue> findByRetryId(@Param("retryId") String retryId, @Param("retryType") String retryType);

    List<RetryQueue> findPage2BeCallback(@Param("pageParam") PageParam pageParam);

    int updateByRetryId4Retry(RetryQueue queue);

    int updateByRetryId4Callback(RetryQueue queue);

    int insertByRetryId4Retry(RetryQueue queue);

}




