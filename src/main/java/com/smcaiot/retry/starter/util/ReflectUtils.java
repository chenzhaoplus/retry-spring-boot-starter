package com.smcaiot.retry.starter.util;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @Author: cz
 * @Date: 2023/7/17
 * @Description:
 */
@Slf4j
public class ReflectUtils {

    public static <T> T invokeMethod(Class<?> clz, String methodName, Object... args) {
        if (Objects.isNull(clz) || StrUtil.isBlank(methodName)) {
            return null;
        }
        Object bean = SpringContext.getBean(clz);
        Method method = ReflectUtil.getMethodByName(bean.getClass(), methodName);
        if (method == null) {
            log.warn("未找到指定方法：{}", bean.getClass().getName() + "." + methodName);
            return null;
        }
        return ReflectUtil.invoke(bean, method, args);
    }

    public static Class<?> getClass(String clzName) {
        try {
            return Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            log.warn("未找到指定类：{}", clzName);
            return null;
        }
    }

    public static String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getName();
    }

    public static String getMethodClass(ProceedingJoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getName();
    }

}
