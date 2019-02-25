package com.leyou.search.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-22 12:11
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
    
}
