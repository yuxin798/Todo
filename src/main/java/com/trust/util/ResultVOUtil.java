package com.trust.util;

import com.trust.vo.Result;

public class ResultVOUtil {

    public static<T> Result<T> success(String msg, T data){
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
    public static<T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("成功");
        result.setData(data);
        return result;
    }
    public static<T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("成功");
        return result;
    }

    public static<T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.setCode("400");
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static<T> Result<T> error(String code, String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
