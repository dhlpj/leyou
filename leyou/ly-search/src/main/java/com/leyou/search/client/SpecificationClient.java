package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-22 13:18
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
