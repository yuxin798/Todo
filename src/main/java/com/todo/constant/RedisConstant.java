package com.todo.constant;

public class RedisConstant {
    // 通过验证码key获取验证码code
    public static final String USER_EMAIL_CODE = "user:email:code:";
    // 通过邀请码获得roomId
    public static final String ROOM_INVITATION_CODE = "room:invitation:code:";
    // 通过roomId获得邀请码
    public static final String ROOM_INVITATION_ID = "room:invitation:id:";

    // 用户申请加入自习室
    public static final String ROOM_REQUEST_JOIN = "room:request:join:";

    // 用户的统计信息
    public static final String USER_STATISTIC = "user:statistic:";

    // 单个任务的统计信息
    public static final String TASK_STATISTIC = "task:statistic:";
}
