package com.itheima.reggie.common;

/**
 * @description：自定义业务异常类
 * @author： jinji
 * @create： 2023/4/12 21:44
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
