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
 * 重试队列表
 *
 * @TableName retry_queue
 */
@TableName(value = "retry_queue")
@Data
@Accessors(chain = true)
public class RetryQueue implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 重试id
     */
    @ApiModelProperty("重试id")
    private String retryId;

    /**
     * 重试类型
     */
    @ApiModelProperty("重试类型")
    private String retryType;

    /**
     * 重试次数
     */
    @ApiModelProperty("重试次数")
    private int retryTimes;

    /**
     * 重试状态：1=待重试；2=待回调；3=已完成
     */
    @ApiModelProperty("重试状态：1=待重试；2=待回调；3=已完成")
    private String retryStatus;

    /**
     * 重试类名
     */
    @ApiModelProperty("重试类名")
    private String retryClass;

    /**
     * 重试方法
     */
    @ApiModelProperty("重试方法")
    private String retryMethod;

    /**
     * 重试参数
     */
    @ApiModelProperty("重试参数")
    private String retryParams;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date crtTime;

    /**
     * 最近一次重试时间
     */
    @ApiModelProperty("最近一次重试时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date lastTime;

    /**
     * 下一次重试时间
     */
    @ApiModelProperty("下一次重试时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date nextTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
