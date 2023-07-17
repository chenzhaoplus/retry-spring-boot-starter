package com.smcaiot.retry.starter.entity;

import com.smcaiot.retry.starter.util.PageParam;
import lombok.Data;

/**
 * @Author: cz
 * @Date: 2023/7/17
 * @Description:
 */
@Data
public class FindPage2BeRetriedParam extends PageParam {

    /**
     * 不执行重试的类型，默认所有类型都重试
     */
    private String[] stopRetryTypes;

}
