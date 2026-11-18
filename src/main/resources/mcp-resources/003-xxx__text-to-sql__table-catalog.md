# 表目录（示例）

> 示例 Resource：Text-to-SQL 场景下的表目录与关系速查，演示模型如何先读目录再决定查哪张表。
> 内容为教学用简化模型，与本仓库代码无耦合。

## 一、按主题分组

### 用户与权限

| 表 | 说明 |
| --- | --- |
| `student` | 学员主体 |
| `sys_user` | 员工账号 |
| `sys_role` / `sys_user_role` | 角色与授权关系 |
| `sys_permission` / `sys_role_permission` | 权限点与角色权限 |

### 课程与教学

| 表 | 说明 |
| --- | --- |
| `sys_course` | 课程定义 |
| `sys_class` | 班级 |
| `sys_class_schedule` | 排课与消课记录 |
| `homework` / `homework_submission` | 作业与提交 |
| `exam` / `exam_question` / `student_exam_record` | 考试、题目与成绩 |

### 交易

| 表 | 说明 |
| --- | --- |
| `prepay_order` | 预订单 |
| `order_payment` | 支付流水 |
| `order_product` | 订单商品明细 |
| `order_refund` | 退费申请与审核 |

### 组织

| 表 | 说明 |
| --- | --- |
| `sys_campus` | 校区 |
| `sys_config` / `sys_config_item` | 系统配置与字典项 |

## 二、常见关联路径

```
学员 → 购买 → 排课 → 作业/考试
student.id = student_product.student_id
student_product.id = sys_class_schedule.student_product_id
student.id = homework_submission.student_id
```

```
订单 → 支付 → 退费
prepay_order.id = order_payment.order_id
order_payment.id = order_refund.payment_id
```

## 三、查询约束

1. 所有业务表都有 `is_deleted`，查询必须显式过滤。
2. 跨校区查询必须带 `campus_id`，否则视为越权。
3. 涉及金额的字段统一为「分」为单位的 BIGINT，展示时再除 100。
4. 手机号、身份证号等 PII 字段不进入最终回答的原文，按脱敏规则输出。
