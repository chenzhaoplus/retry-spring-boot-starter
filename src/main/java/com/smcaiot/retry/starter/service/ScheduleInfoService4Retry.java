package com.smcaiot.retry.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smcaiot.retry.starter.entity.ScheduleInfo;

import java.util.List;

/**
 *
 */
public interface ScheduleInfoService4Retry extends IService<ScheduleInfo> {

    List<ScheduleInfo> findTaskByTypes(List<String> taskTypes);

    List<ScheduleInfo> findOpenTasks();

}
