-- =====================================================
-- 节点组相关表结构
-- =====================================================

USE `thor`;

-- 节点组表
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