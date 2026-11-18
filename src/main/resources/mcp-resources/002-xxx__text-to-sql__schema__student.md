# student 表 DDL（示例）

> 示例 Resource：演示 Bear MCP 的 `bear://xxx/...` 资源命名与 DDL 文档格式。
> 该表只存在于演示库，与本仓库运行环境无关。

```sql
CREATE TABLE `student` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学员主键',
  `student_no`    VARCHAR(32)  NOT NULL COMMENT '学员编号，业务唯一键',
  `name`          VARCHAR(64)  NOT NULL COMMENT '学员姓名',
  `mobile`        VARCHAR(20)      NULL COMMENT '手机号，输出需脱敏',
  `gender`        TINYINT          NULL COMMENT '性别：1=男，2=女，0=未知',
  `campus_id`     BIGINT       NOT NULL COMMENT '归属校区 ID',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=潜在客户，1=在读，2=休学，3=退费结案',
  `advisor_id`    BIGINT           NULL COMMENT '跟进人（员工）ID',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_campus_status` (`campus_id`, `status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员主表';
```

## 索引与查询建议

- 按校区 + 状态过滤走 `idx_campus_status`，不要只给 `status` 建单列条件。
- 按时间段统计走 `idx_create_time`，时间范围建议左闭右开：`>= '2026-01-01' AND < '2026-02-01'`。
- 逻辑删除必须显式加 `is_deleted = 0`，仓储层不会自动追加。
