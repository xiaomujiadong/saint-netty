package com.saint.netty.entity;

import com.alibaba.fastjson.JSONObject;
import com.saint.netty.constant.CodeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @date: 2019/9/9 19:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MsgReturn<T> {
    private int code;
    private String msg;
    private T data;

    public MsgReturn(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public static MsgReturn renderSuccess() {
        return renderSuccess((Object)null);
    }

    public static <T> MsgReturn<T> renderSuccess(T data) {
        return renderSuccess(data, CodeStatusEnum.SUCCESS.getMsg());
    }

    public static <T> MsgReturn<T> renderSuccess(T data, String msg) {
        return new MsgReturn(CodeStatusEnum.SUCCESS.getCode(), msg, data);
    }

    public static MsgReturn renderFailure() {
        return renderFailure(CodeStatusEnum.FAIL.getMsg());
    }

    public static MsgReturn renderFailure(String msg) {
        return renderFailure(CodeStatusEnum.FAIL.getCode(), msg);
    }

    public static MsgReturn renderFailure(CodeStatusEnum CodeStatusEnum) {
        return renderFailure(CodeStatusEnum.getCode(), CodeStatusEnum.getMsg());
    }

    public static MsgReturn renderFailure(int customizeCode, String msg) {
        return new MsgReturn(customizeCode, msg, (Object)null);
    }
}
