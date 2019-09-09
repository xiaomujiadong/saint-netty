package com.saint.netty.constant;

/**
 * @description:
 * @date: 2019/9/9 20:28
 */
public enum CodeStatusEnum {
    FAIL(0, "错误"),

    SUCCESS(200, "success"),

    PARAMS_ERROR(400, "参数错误")
    ;

    private int code;
    private String msg;

    CodeStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
