package com.leyou.auth.client;

import com.leyou.user.pojo.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-02-18 11:35
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
