package com.gate.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ：huang_fengge
 * @date ：Created in 2021/12/29 上午9:19
 * @description：启动类
 * @modified By：
 * @version: 1.0.0$
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(App.class);
        springApplication.run(args);
    }
}
