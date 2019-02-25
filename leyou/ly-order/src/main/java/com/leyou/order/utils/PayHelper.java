package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayConfig config;

    /**
     * 创建订单
     * @param orderId
     * @param totalPay
     * @param desc
     * @return
     */
    public String createOrder(Long orderId,Long totalPay,String desc){
        Map<String,String> data = new HashMap<>();
        //商品描述
        data.put("body",desc);
        //订单号
        data.put("out_trade_no",orderId.toString());
        //金额
        data.put("total_fee",totalPay.toString());
        //调用微信支付的终端api
        data.put("spbill_create_ip","127.0.0.1");
        //回调地址
        data.put("notify_url", config.getNotifyUrl());
        //交易类型为扫码支付
        data.put("trade_type", "NATIVE");

        //利用wxpay,完成下单
        try {
            Map<String,String> result = wxPay.unifiedOrder(data);
            //判断通信及业务结果
            isSuccessReceived(result);
            String code_url = result.get("code_url");
            return code_url;
        } catch (Exception e) {
            log.error("[微信下单]创建预交易订单失败,orderId:{}",orderId,e);
            return null;
        }
    }

    /**
     * 判断通信结果及业务结果
     * @param result
     */
    public void isSuccessReceived(Map<String, String> result) {
        //判断通信标识
        String return_code = result.get("return_code");
        if(return_code.equals(WXPayConstants.FAIL)){
            log.error("[微信下单]微信下单预交易通信失败,失败原因:{}",result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_CONNECTION_FAIL);
        }
        //判断业务结果
        String result_code = result.get("result_code");
        if(result_code.equals(WXPayConstants.FAIL)){
            log.error("[微信下单]微信下单业务失败,错误码:{},失败原因:{}",
                    result.get("err_code"),result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_CONNECTION_FAIL);
        }
    }

    /**
     * 判断签名
     * @param map
     */
    public void isValidSignature(Map<String, String> map) {
        //判断签名是否正确
        try {
            String signature1 = WXPayUtil.generateSignature(map, config.getKey(), WXPayConstants.SignType.MD5);
            String signature2 = WXPayUtil.generateSignature(map, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign = map.get("sign");
            if (!StringUtils.equals(signature1, sign) && !StringUtils.equals(signature2, sign)) {
                throw new LyException(ExceptionEnum.INVALID_SIGNATURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询订单状态
     * @param id
     * @return
     */
    public Map<String, String> queryPayStatus(Long id) {
        // 组织请求参数
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", id.toString());
        // 查询状态
        try {
            Map<String, String> result = wxPay.orderQuery(data);
            isSuccessReceived(result);
            isValidSignature(result);
            return result;
        } catch (Exception e) {
            log.error("【订单查询】订单查询失败，orderId：{}",id,e);
            throw new LyException(ExceptionEnum.ORDER_STATUS_QUERY_ERROR);
        }

    }
}