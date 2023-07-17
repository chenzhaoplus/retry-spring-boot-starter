package com.smcaiot.retry.starter.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: cz
 * @Date: 2023/6/30
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum RetryStatus {

    to_be_retry("待重试", "1"),
    to_be_callback("待回调", "2"),
    finished("已完成", "3");

    private String title;
    private String code;

}
