package com.smcaiot.retry.starter.app;

import com.smcaiot.retry.starter.service.ScheduleInfoService4Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * 多线程执行定时任务
 */
@Configuration
public class ScheduleConfig4Retry implements SchedulingConfigurer {

    @Autowired
    private ScheduleInfoService4Retry scheduleInfoService4Retry;
    @Autowired
    private RetryProperties retryProp;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //List<ScheduleInfo> tasks = scheduleInfoService.findOpenTasks();
        //taskRegistrar.setScheduler(Executors.newScheduledThreadPool(CollUtil.isEmpty(tasks) ? 1 : tasks.size()));
        int cnt = Optional.ofNullable(retryProp.getScheduleCount()).orElse(2);
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(cnt));
    }

}
