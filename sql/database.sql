-- =====================================================
-- Thor 雷神分布式文件调度与传输控制系统
-- MySQL 5.7.30 完整建库建表脚本 (thor_ 前缀)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `thor_db`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `thor_db`;

-- 1. 应用系统表 (thor_app_system)
DROP TABLE IF EXISTS `thor_app_system`;
CREATE TABLE `thor_app_system` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_code` VARCHAR(50) NOT NULL COMMENT '应用系统编码（唯一）',
  `app_name` VARCHAR(100) NOT NULL COMMENT '应用系统名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` VARCHAR(50) DEFAULT NULL,
  `update_by` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用系统配置表';

-- 2. 节点表 (thor_node)
DROP TABLE IF EXISTS `thor_node`;
CREATE TABLE `thor_node` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `node_name` VARCHAR(64) NOT NULL,
  `node_type` VARCHAR(32) DEFAULT 'NODE',
  `ip_address` VARCHAR(64) NOT NULL,
  `port` INT NOT NULL DEFAULT 5599,
  `status` VARCHAR(32) DEFAULT 'ONLINE',
  `last_heartbeat` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_name` (`node_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群节点心跳表';

-- 3. 任务路由配置表 (thor_task_cfg)
DROP TABLE IF EXISTS `thor_task_cfg`;
CREATE TABLE `thor_task_cfg` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_group` VARCHAR(64) DEFAULT NULL,
  `src_node` VARCHAR(64) DEFAULT NULL,
  `dst_node` VARCHAR(64) NOT NULL,
  `file_pattern` VARCHAR(255) NOT NULL,
  `process_type` VARCHAR(32) DEFAULT 'DIRECT',
  `from_charset` VARCHAR(32) DEFAULT NULL,
  `to_charset` VARCHAR(32) DEFAULT NULL,
  `is_active` TINYINT DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_file_pattern` (`file_pattern`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务路由与处理策略表';

-- 4. 任务流水实例表 (thor_task_instance)
DROP TABLE IF EXISTS `thor_task_instance`;
CREATE TABLE `thor_task_instance` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `task_id` VARCHAR(64) NOT NULL,
  `cfg_id` BIGINT UNSIGNED DEFAULT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `total_size` BIGINT DEFAULT 0,
  `status` VARCHAR(32) NOT NULL,
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `error_msg` TEXT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核心任务流水账单';