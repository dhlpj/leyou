package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 该应用只用于处理消息队列中的消息，所以不需要使用Eureka等服务发现组件
 * @author Jack
 * @create 2019-02-13 19:07
 */
@SpringBootApplication
public class LySmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySmsApplication.class,args);
    }
}
