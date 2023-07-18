package com.smcaiot.retry.starter.task;

import cn.hutool.core.util.StrUtil;
import com.smcaiot.retry.starter.entity.ScheduleInfo;
import com.smcaiot.retry.starter.service.ScheduleInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.Objects;

import static com.smcaiot.retry.starter.constants.Constants.PDBS_YES;

/**
 * @Author: cz
 * @Date: 2023/7/12
 * @Description:
 */
@Slf4j
public abstract class AbstractTask implements SchedulingConfigurer {

    @Autowired
    protected ScheduleInfoService scheduleInfoService;

    protected abstract void initTaskRunnable(ScheduleInfo schedule);

    protected abstract List<ScheduleInfo> getSchedules();

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        getSchedules().stream().forEach(schedule -> addTask(schedule, taskRegistrar));
    }

    private void addTask(ScheduleInfo schedule, ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                () -> runTask(schedule.getId()),
                triggerContext -> {
                    ScheduleInfo latestSchedule = scheduleInfoService.getById(schedule.getId());
                    if (Objects.isNull(latestSchedule)) {
                        return null;
                    }
                    if (StrUtil.isBlank(latestSchedule.getScheduleCron())) {
                        log.warn("任务`{}`的cron不能为空，请配置！", latestSchedule.getScheduleName());
                        return null;
                    }
                    return new CronTrigger(latestSchedule.getScheduleCron()).nextExecutionTime(triggerContext);
                });
    }

    protected void runTask(Integer id) {
        ScheduleInfo schedule = scheduleInfoService.getById(id);
        try {
            if (!PDBS_YES.equals(schedule.getScheduleOpen())) {
                log.warn("任务`{}`的开关是关闭的！", schedule.getScheduleName());
                return;
            }
            initTaskRunnable(schedule);
            long begin = System.currentTimeMillis();
            log.debug("开始{}.., cron: {}", schedule.getScheduleName(), schedule.getScheduleCron());
            schedule.getTaskExcute().run();
            long end = System.currentTimeMillis();
            log.debug("结束{}..耗时：{} 秒", schedule.getScheduleName(), ((end - begin) / 1000));
        } catch (Exception e) {
            log.warn("{}失败：{}", schedule.getScheduleName(), e);
        }
    }

}
