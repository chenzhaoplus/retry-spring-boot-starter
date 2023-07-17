package com.smcaiot.retry.starter.entity;

import com.smcaiot.retry.starter.util.PageParam;
import lombok.Data;

/**
 * @Author: cz
 * @Date: 2023/7/17
 * @Description:
 */
@Data
public class FindPage2BeCallbackParam extends PageParam {

    /**
     * 不执行回调的类型，默认所有类型都回调
     */
    private String[] stopCallbackTypes;

}
