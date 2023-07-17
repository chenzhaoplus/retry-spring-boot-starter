package com.smcaiot.retry.starter.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smcaiot.retry.starter.entity.ScheduleInfo;
import com.smcaiot.retry.starter.mapper.ScheduleInfoMapper;
import com.smcaiot.retry.starter.service.ScheduleInfoService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class ScheduleInfoServiceImpl extends ServiceImpl<ScheduleInfoMapper, ScheduleInfo>
        implements ScheduleInfoService {

    @Override
    public List<ScheduleInfo> findTaskByTypes(List<String> taskTypes) {
        if(CollUtil.isEmpty(taskTypes)){
            return new ArrayList<>();
        }
        List<ScheduleInfo> list = getBaseMapper().findTaskByTypes(taskTypes);
        return CollUtil.isEmpty(list) ? new ArrayList<>() : list;
    }

    @Override
    public List<ScheduleInfo> findOpenTasks() {
        List<ScheduleInfo> list = getBaseMapper().findOpenTasks();
        return CollUtil.isEmpty(list) ? new ArrayList<>() : list;
    }

}




