package com.leyou.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成订单时需要使用到的DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long skuId;// 商品skuId
    private Integer num;// 购买数量
}