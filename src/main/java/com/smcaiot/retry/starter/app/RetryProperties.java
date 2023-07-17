package com.smcaiot.retry.starter.app;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = RetryProperties.PREFIX)
@Data
@Slf4j
public class RetryProperties {

    public static final String PREFIX = "retry";

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;
    /**
     * 一次重试多少条记录
     */
    private Integer retryPageSize;
    /**
     * 一次重试回调多少条记录
     */
    private Integer callbackPageSize;
    /**
     * 重试定时任务数量
     */
    private Integer scheduleCount;
    /**
     * 不执行重试的类型，默认所有类型都重试
     */
    private String[] stopRetryTypes;
    /**
     * 不执行回调的类型，默认所有类型都回调
     */
    private String[] stopCallbackTypes;

}
