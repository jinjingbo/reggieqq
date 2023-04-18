package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/10 22:36
 */
@Slf4j
@SpringBootApplication
@ServletComponentScan///拦截器扫描注解
@EnableTransactionManagement//d多表操作加事务
public class ReggieApplication {
    public static void main (String[] args){
        SpringApplication.run(ReggieApplication.class,args);
        log.info("succeed");
    }
}
