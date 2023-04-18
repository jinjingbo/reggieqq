package com.itheima.reggie.common;

/**
 * @description：基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 * @author： jinji
 * @create： 2023/4/12 16:56
 */
//线程工具类
public class BaseContext {
    //工具栏，static写死
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置值
     * @param id
     */
    //在最开始的拦截器中,把需要的内容存在
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
