package com.smcaiot.retry.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smcaiot.retry.starter.entity.ScheduleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.smcaiot.ibmp.police.entity.ScheduleInfo
 */
public interface ScheduleInfoMapper4Retry extends BaseMapper<ScheduleInfo> {

    List<ScheduleInfo> findTaskByTypes(@Param("taskTypes") List<String> taskTypes);

    List<ScheduleInfo> findOpenTasks();

}




