package com.lab.face.common;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String msg;
    private Object data;

    public static Result success() {
        Result r = new Result();
        r.setCode(200);
        r.setMsg("成功");
        return r;
    }

    public static Result success(Object data) {
        Result r = new Result();
        r.setCode(200);
        r.setMsg("成功");
        r.setData(data);
        return r;
    }

    public static Result error(String msg) {
        Result r = new Result();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    public static Result error(int code, String msg) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
