package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * @author Jack
 * @create 2018-11-25 19:56
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空！"),//客户端参数有误
    CATEGORY_NOT_FOUND(404,"没有查询到商品分类！"),
    BRAND_NOT_FOUND(404,"没有查询到对应的品牌！"),
    BRAND_SAVE_ERROR(500,"品牌新增失败！"),
    INVALID_FILE_TYPE(400,"文件类型无效！"),
    UPLOAD_FILE_ERROR(404,"上传文件失败！！"),
    SPEC_GROUP_NOT_FOUND(404,"没有商品规格参数组的信息！"),
    SPEC_PARAM_NOT_FOUND(404,"没有规格参数的信息！"),
    GOODS_NOT_FOUND(404,"没有商品的信息！"),
    GOODS_SAVE_ERROR(500,"新增商品失败！"),
    GOODS_DETAIL_NOT_FOUND(404,"没有查询到商品详情！"),
    GOODS_SKU_NOT_FOUND(404,"没有查询到商品SKU！"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品id不能为空！"),
    GOODS_UPDATE_ERROR(500,"商品更新失败！"),
    INVALID_USER_DATA_TYPE(400,"用户参数类型无效！"),
    INVALID_VERIFY_CODE(400,"验证码无效！"),
    INVALID_USERNAME_PASSWORD(400,"用户名或密码错误！"),
    CREATE_TOKEN_ERROR(500,"生成token失败！"),
    UNAUTHORIZED(403,"用户未登录！"),
    CART_NOT_FOUND(404,"未找到购物车信息！"),
    CREATE_ORDER_ERROR(500,"订单生成失败！"),
    STOCK_NOT_ENOUGH(500,"商品库存不足！"),
    ORDER_NOT_FOUND(500,"订单不存在！"),
    ORDER_DETAIL_NOT_FOUND(500,"订单详情不存在！"),
    ORDER_STATUS_NOT_FOUND(500,"订单状态不存在！"),
    WX_PAY_CONNECTION_FAIL(500,"微信连接失败！"),
    ORDER_STATUS_ERROR(400,"订单状态异常！"),
    INVALID_SIGNATURE(400,"签名无效！"),
    INVALID_ORDER_PARAM(400,"参数有误！"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单失败！"),
    ORDER_STATUS_QUERY_ERROR(500,"订单状态查询失败！");
    private int code;
    private String msg;
}
