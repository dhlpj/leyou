package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-22 11:30
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {

}
