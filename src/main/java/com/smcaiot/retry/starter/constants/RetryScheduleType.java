package com.smcaiot.retry.starter.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: cz
 * @Date: 2023/7/11
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum RetryScheduleType {

    callback("通知回调"),
    retry("通知重试");

    private String title;

}
