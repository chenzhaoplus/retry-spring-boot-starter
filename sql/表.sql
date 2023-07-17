-- 重试队列
CREATE TABLE `retry_queue`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `retry_id`     varchar(64) NOT NULL COMMENT '重试id',
    `retry_type`   varchar(50) NOT NULL COMMENT '重试类型',
    `retry_times`  int(8) DEFAULT 0 COMMENT '重试次数',
    `retry_status` varchar(2)  DEFAULT '1' COMMENT '重试状态：1=待重试；2=待回调；3=已完成',
    `retry_class` varchar(200) DEFAULT NULL COMMENT '重试类名',
    `retry_method` varchar(50) DEFAULT NULL COMMENT '重试方法',
    `retry_params` text COMMENT '重试参数',
    `crt_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `last_time`    timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近一次重试时间',
    `next_time`    timestamp NULL DEFAULT NULL COMMENT '下一次重试时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='重试队列表';

-- 定时任务配置
CREATE TABLE `schedule_info`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `schedule_name` varchar(64) DEFAULT NULL COMMENT '任务名称',
    `schedule_type` varchar(50) DEFAULT NULL COMMENT '任务类型',
    `schedule_open` varchar(2)  DEFAULT NULL COMMENT '任务开关：1=开启；2=关闭；',
    `schedule_cron` varchar(20) DEFAULT NULL COMMENT '任务cron',
    `crt_time`      timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='定时任务配置表';

INSERT INTO `schedule_info`(`schedule_name`, `schedule_type`, `schedule_open`, `schedule_cron`, `crt_time`) VALUES ('重试', 'retry', '1', '0 */1 * * * ?', now());
INSERT INTO `schedule_info`(`schedule_name`, `schedule_type`, `schedule_open`, `schedule_cron`, `crt_time`) VALUES ('回调', 'callback', '1', '0 */1 * * * ?', now());
