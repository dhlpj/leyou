package com.leyou.order.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-02-21 14:16
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
