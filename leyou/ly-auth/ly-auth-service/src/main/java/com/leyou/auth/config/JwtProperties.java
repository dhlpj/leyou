package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author Jack
 * @create 2019-02-18 10:39
 */
@Data
@ConfigurationProperties("ly.jwt")
public class JwtProperties {
    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;

    private PublicKey publicKey;//公钥
    private PrivateKey privateKey;//私钥

    //对象初始化完成时执行
    @PostConstruct
    public void init() throws Exception {
        File pubPath = new File(pubKeyPath);
        File priPath = new File(priKeyPath);
        if(!pubPath.exists()||!priPath.exists()){//生成公钥
            RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
        }
        //获取公钥，私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }
}
