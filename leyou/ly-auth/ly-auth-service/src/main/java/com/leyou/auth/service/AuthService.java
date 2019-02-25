package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * @author Jack
 * @create 2019-02-18 11:04
 */
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;

    public String login(String username, String password) {
        User user;
        //校验用户名和密码
        try{
            user = userClient.queryUserByUsernameAndPassword(username, password);
        }catch (Exception e){
            log.error("[授权中心]用户登录失败，用户名称:{}",username,e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //判断
        if(user==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        try {
            //生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()), jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            log.error("[授权中心]TOKEN生成失败,用户名称:{}",username,e);
            throw new LyException(ExceptionEnum.CREATE_TOKEN_ERROR);
        }
    }
}
