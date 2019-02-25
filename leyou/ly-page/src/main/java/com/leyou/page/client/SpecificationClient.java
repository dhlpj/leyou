package com.leyou.page.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Jack
 * @create 2019-01-29 16:14
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
