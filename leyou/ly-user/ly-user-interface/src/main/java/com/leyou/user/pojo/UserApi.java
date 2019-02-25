package com.leyou.user.pojo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Jack
 * @create 2019-02-18 11:35
 */
public interface UserApi {
    /**
     * 用户名密码验证
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    User queryUserByUsernameAndPassword(@RequestParam("username") String username, @RequestParam("password") String password);
}
