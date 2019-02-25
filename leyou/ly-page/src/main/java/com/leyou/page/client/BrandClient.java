package com.leyou.page.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-29 16:13
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
