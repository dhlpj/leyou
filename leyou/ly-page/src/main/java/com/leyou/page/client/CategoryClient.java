package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-29 16:10
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {

}
