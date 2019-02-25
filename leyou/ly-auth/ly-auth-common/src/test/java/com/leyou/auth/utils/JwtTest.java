package com.leyou.auth.utils;

import com.leyou.auth.pojo.UserInfo;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;


/**
 * @author Jack
 * @create 2019-02-15 16:45
 */
public class JwtTest {
    private static final String pubKeyPath = "/Users/jie.peng/Documents/rsa.pub";

    private static final String priKeyPath = "/Users/jie.peng/Documents/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MzEsImV4cCI6MTU1MDQ3Mjk3MX0.iPCY06d_ztQ3CD4z7j6rx4AGtnjFRZz09R28YUeLLtuviDNtjOklpvHjdL5BMiV1e4Ea29Mk9VLNt6roVw_Qfe1cBrV2nBQNCUCgx6LQ_AaJHhMuxOmdmy6LTCnoWASuewXj30IJNKlIr1XybcK4Zu_3dxWuE_DJg5Dk2S-LdbU";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}