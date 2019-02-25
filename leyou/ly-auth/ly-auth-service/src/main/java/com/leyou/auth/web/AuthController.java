package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jack
 * @create 2019-02-18 11:05
 */
@EnableConfigurationProperties(JwtProperties.class)
@RestController
public class AuthController {
    @Autowired
    private AuthService authService;
    @Value("${ly.jwt.cookieName}")
    private String cookieName;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                      HttpServletRequest request, HttpServletResponse response){
        String token = authService.login(username,password);
        //设置cookie,request用来设置domain
        CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verifyUser(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request, HttpServletResponse response){
        //未登录
        if (StringUtils.isBlank(token)) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        try{
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //刷新token
            JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            //token过期或被篡改
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
