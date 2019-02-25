package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-22 13:20
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
