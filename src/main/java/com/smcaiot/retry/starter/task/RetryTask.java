package com.smcaiot.retry.starter.task;

import com.smcaiot.retry.starter.constants.RetryScheduleType;
import com.smcaiot.retry.starter.entity.ScheduleInfo;
import com.smcaiot.retry.starter.service.RetryQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: cz
 * @Date: 2023/7/12
 * @Description:
 */
@Component
@EnableScheduling
@Slf4j
public class RetryTask extends AbstractTask {

    @Autowired
    private RetryQueueService retryQueueService;

    @Override
    public void initTaskRunnable(ScheduleInfo schedule) {
        switch (RetryScheduleType.valueOf(schedule.getScheduleType())) {
            case retry:
                schedule.setTaskExcute(retryQueueService::retry);
                break;
            case callback:
                schedule.setTaskExcute(retryQueueService::callback);
                break;
            default:
                throw new RuntimeException("不支持的任务类型");
        }
    }

    @Override
    protected List<String> getScheduleTypes() {
        return Arrays.stream(RetryScheduleType.values()).map(type -> type.name())
                .collect(Collectors.toList());
    }

}
