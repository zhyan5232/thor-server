-- =====================================================
-- Thor 雷神分布式文件调度与传输控制系统
-- MySQL 5.7.30 完整建库建表脚本 (thor_ 前缀)
-- =====================================================

CREATE DATABASE IF NOT EXISTS `thor`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `thor`;

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

-- 2. 节点组表 (thor_node_group) - 新增
DROP TABLE IF EXISTS `thor_node_group`;
CREATE TABLE `thor_node_group` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `app_system_id` BIGINT UNSIGNED NOT NULL COMMENT '所属应用系统ID',
  `group_code` VARCHAR(50) NOT NULL COMMENT '节点组编码（全局唯一）',
  `group_name` VARCHAR(100) NOT NULL COMMENT '节点组名称',
  `status` TINYINT DEFAULT 1 COMMENT '1-启用 0-停用',
  `description` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` VARCHAR(50) DEFAULT NULL,
  `update_by` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`),
  KEY `idx_app_system_id` (`app_system_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点组表';

-- 3. 节点组与节点关联表 (thor_node_group_node)
DROP TABLE IF EXISTS `thor_node_group_node`;
CREATE TABLE `thor_node_group_node` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `node_group_id` BIGINT UNSIGNED NOT NULL,
  `node_id` BIGINT UNSIGNED NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_node` (`node_group_id`, `node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点组与节点关联表';

-- 4. 节点表 (thor_node)
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