package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PublicKey;

@Data
@ConfigurationProperties("ly.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;//公钥

    //对象初始化完成时执行
    @PostConstruct
    public void init() throws Exception {
        File pubPath = new File(pubKeyPath);
        //获取公钥，私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
}
