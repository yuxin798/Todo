package com.todo.constant;

public class RedisConstant {
    // 通过验证码key获取验证码code
    public static final String EMAIL_VALIDATE_CODE = "user:email:code:";
    // 通过邀请码获得roomId
    public static final String ROOM_INVITATION_CODE = "room:invitation:code:";
    // 通过roomId获得邀请码
    public static final String ROOM_INVITATION_ID = "room:invitation:id:";
}
