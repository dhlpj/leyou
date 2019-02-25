package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Jack
 * @create 2019-02-19 17:45
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LyCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(LyCartApplication.class,args);
    }
}
