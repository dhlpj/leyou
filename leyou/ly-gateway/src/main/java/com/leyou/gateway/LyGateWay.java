package com.leyou.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author Jack
 * @create 2018-11-23 12:56
 */
@EnableDiscoveryClient
@EnableZuulProxy//该注解使用Ribbon来定位注册在Eureka Server中的微服务，同时也整合了Hystrix（里面的@EnableCircuitBreaker），
                // 所有的zuul请求都会在hystrix命令中执行。
@SpringBootApplication
//视屏使用的是@SpringCloud注解
public class LyGateWay {
    public static void main(String[] args) {
        SpringApplication.run(LyGateWay.class);
    }
}
