导航栏
1. 主页（今日待办）
	1. 日历
	1. 日程表（当日的今日待办）
1. 活动（清单列表）
1. 记录
	1. 时间轴
	2. 图表
2. 自习室
3. 个人

## 数据库
**用户表 user**
bigint  user_id
varchar(255)  username
varchar(255)  password
varchar(255)  avatar
timestamp  created_at
timestamp  updated_at
timestamp  deleted

**任务表 task**
bigint  task_id
bigint  user_id
varchar(255)  task_name

> 预估番茄数
> 2,3,1,3,2

int estimate

> 实际进行的番茄数

int  tomato_clock_times

> 停止的番茄数

int  stop_times

> status
> 0 今日待办
> 1 清单列表
> 2 已完成
> 3 已删除

tinyint  task_status

int  inner_interrupt
int  outter_interrupt

timestamp started_at
timestamp completed_at

timestamp  created_at
timestamp  updated_at
timestamp  deleted

**番茄钟 tomato_clock**
bigint  clock_id
bigint  task_id
int  sequece

> status
>
> 0 已完成
>
> 1 正在进行
>
> 2 未开始
>
> 3 已停止

tinyint  task_status

varchar(255) stop_reason

int  inner_interrupt
int  outter_interrupt

timestamp started_at
timestamp completed_at

timestamp  created_at
timestamp  updated_at
timestamp  deleted

**自习室表 room**
bigint  room_id

==bigint  user_id==

varchar(255)  room_name
varchar(255)  room_avatar
timestamp  created_at
timestamp  updated_at
timestamp  deleted

**用户 自习室 关系表 user_room**
bigint  user_room_id
bigint  user_id
bigint  room_id
timestamp  created_at
timestamp  updated_at
timestamp  deleted