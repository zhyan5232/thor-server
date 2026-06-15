# Thor Server

Thor 雷神分布式文件调度与传输控制系统后端服务器

## 项目结构

```
thor-server/
├── thor-common/       # 公共实体、Mapper、枚举
├── thor-admin-api/    # Web控制面接口层 (与前端对接)
└── thor-node/         # 底层传输引擎 (Netty + Zero-Copy)
```

## 快速开始

```bash
# 1. 执行数据库脚本
mysql -u root -p < sql/database.sql

# 2. 修改 thor-admin-api/src/main/resources/application.yml 中的数据库配置

# 3. 启动 thor-admin-api
```