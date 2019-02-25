package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.dto.CartDTO;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jack
 * @create 2019-02-21 10:52
 */
@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long generateOrder(OrderDTO orderDTO) {
        //1、新增订单
        Order order = new Order();
        //1.1、生成orderId
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        order.setSourceType(2);//从哪儿下单
        order.setInvoiceType(0);//发票类型
        order.setPromotionIds(null);//促销信息
        //1.2、买家信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        order.setBuyerMessage(null);
        //1.3、收货人信息
        AddressDTO address = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(address.getName());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverZip(address.getAddress());
        //1.4、金额
        Map<Long, Integer> skuIdAndNum = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获得sku的ids
        Set<Long> ids = skuIdAndNum.keySet();
        ArrayList<Long> idList = new ArrayList<>(ids);
        //查询sku
        List<Sku> skus = goodsClient.querySkuByIds(idList);
        List<OrderDetail> orderDetails = new ArrayList<>();
        long totalPay = 0;
        for (Sku sku : skus) {
            Integer num = skuIdAndNum.get(sku.getId());
            totalPay=totalPay+sku.getPrice()*num;
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNum(num);
            orderDetail.setSkuId(sku.getId());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));//获取第一张图片
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setTitle(sku.getTitle());
            orderDetails.add(orderDetail);
        }
        long postFee = 0;//邮费
        long promotionFee = 0;//优惠价格
        order.setPostFee(postFee);
        order.setTotalPay(totalPay+postFee);
        order.setActualPay(totalPay+postFee-promotionFee);
        //1.5 物流
        order.setShippingName(null);
        order.setShippingCode(null);
        int count = orderMapper.insert(order);
        if (count!=1){
            log.error("[订单服务]新增订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //2、新增订单详情
        count = orderDetailMapper.insertList(orderDetails);
        if(count!=orderDetails.size()){
            log.error("[订单服务]新增订单详情失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //3、TODO 新增订单状态其他属性
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if(count!=1){
            log.error("[订单服务]新增订单状态失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //4、删除购物车中部分商品(将skuId加入到mq中，由cart服务自己处理)
        Map<Long,List<Long>> userIdAndSkuId = new HashMap<>();
        userIdAndSkuId.put(user.getId(),idList);
        amqpTemplate.convertAndSend("cart.sku.delete",userIdAndSkuId);
        //5、减库存（此时注意高并发问题）
        goodsClient.decreaseStock(orderDTO.getCarts());
        return orderId;
    }

    /**
     * 根据订单id来查询订单
     * @param id
     * @return
     */
    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if(order==null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(orderDetails)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(id);
        orderStatus = orderStatusMapper.selectOne(orderStatus);
        if (orderStatus==null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    /**
     * 生成二维码的支付链接
     * @param id
     * @return
     */
    public String generateUrl(Long id) {
        Order order = queryOrderById(id);
        Integer status = order.getOrderStatus().getStatus();
        if (status.intValue()!=OrderStatusEnum.UN_PAY.value()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String title = orderDetail.getTitle();
        String url = payHelper.createOrder(id, 1L/*order.getActualPay()*/, title);//TODO modify payment amount
        return url;
    }

    /**
     * 处理支付结果通知
     * @param map
     * @return
     */
    public Map<String,String> handleNotification(Map<String, String> map) {
        log.info("【支付回调】接收微信支付回调，结果:{}",map);
        //数据校验
        payHelper.isSuccessReceived(map);
        //判断签名是否正确
        payHelper.isValidSignature(map);
        //获取订单中的金额
        Long totalFee = Long.valueOf(map.get("total_fee"));
        //获取orderId
        Long orderId = Long.valueOf(map.get("out_trade_no"));
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        //校验订单金额,防止被中间人攻击
        if(totalFee.longValue()!=1/*order.getActualPay()*/){//TODO modify payment amount
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //更新订单状态
        updateOrderStatus(orderId);
        Map<String,String> result = new HashMap<>();
        result.put("return_code","SUCCESS");
        result.put("return_msg","OK");
        log.info("【支付回调】修改订单状态，订单ID:{}",orderId);
        return result;
    }

    /**
     * 更新订单状态
     * @param orderId
     */
    private void updateOrderStatus(Long orderId) {
        //修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setPaymentTime(new Date());
        orderStatus.setStatus(OrderStatusEnum.PAYED.value());
        int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if(count!=1){
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
    }

    /**
     * 查询订单状态
     * @param id
     * @return
     */
    public PayState queryPayStatus(Long id) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if(orderStatus.getStatus().intValue()!=OrderStatusEnum.UN_PAY.value()){//付款成功
            return PayState.SUCCESS;
        }
        //否则，可能服务器未接收到微信数据,查询微信
        Map<String,String> map = payHelper.queryPayStatus(id);
        /**
         * SUCCESS—支付成功
         *
         * REFUND—转入退款
         *
         * NOTPAY—未支付
         *
         * CLOSED—已关闭
         *
         * REVOKED—已撤销（付款码支付）
         *
         * USERPAYING--用户支付中（付款码支付）
         *
         * PAYERROR--支付失败(其他原因，如银行返回失败)
         */
        String state = map.get("trade_state");
        if("SUCCESS".equals(state)){
            updateOrderStatus(id);
            return PayState.SUCCESS;
        }
        if ("NOTPAY".equals(state) || "USERPAYING".equals(state)) {
            return PayState.NOT_PAY;
        }
        return PayState.FAIL;
    }
}