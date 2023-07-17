package com.smcaiot.retry.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: cz
 * @Date: 2023/7/13
 * @Description:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryField {

    boolean retryId() default false;

    boolean retryType() default false;

    boolean doCallback() default false;

}
