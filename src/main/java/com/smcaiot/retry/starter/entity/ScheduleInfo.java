package com.smcaiot.retry.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务配置表
 *
 * @TableName schedule_info
 */
@TableName(value = "schedule_info")
@Data
@Accessors(chain = true)
public class ScheduleInfo implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String scheduleName;

    /**
     * 任务类型
     */
    @ApiModelProperty("任务类型")
    private String scheduleType;

    /**
     * 任务开关：1=开启；2=关闭；
     */
    @ApiModelProperty("任务开关：1=开启；2=关闭；")
    private String scheduleOpen;

    /**
     * 任务cron
     */
    @ApiModelProperty("任务cron")
    private String scheduleCron;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date crtTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("定时任务执行")
    @TableField(exist = false)
    private Runnable taskExcute;

}
