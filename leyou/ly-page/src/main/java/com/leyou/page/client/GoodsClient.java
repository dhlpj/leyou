package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-28 17:34
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
