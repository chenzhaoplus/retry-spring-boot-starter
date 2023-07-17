package com.smcaiot.retry.starter.service;

import com.smcaiot.retry.starter.entity.RetryQueue;

import java.util.List;

/**
 * @Author: cz
 * @Date: 2023/7/13
 * @Description:
 */
public interface RetryCallback {

    boolean doCallback(List<RetryQueue> queues);

}
