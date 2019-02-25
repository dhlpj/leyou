package com.leyou.cart.mq;

import com.leyou.cart.service.CartService;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jack
 * @create 2019-02-22 09:53
 */
@Component
public class CartListener {
    @Autowired
    private CartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.delete.sku.queue",durable = "true"),
            exchange = @Exchange(name = "leyou.cart.exchange",type = ExchangeTypes.TOPIC),
            key = "cart.sku.delete"//指定匹配的routing key（也就是指定队列的binding key）
    ))
    public void listenDelete(Map<Long, List<Long>> msg){
        if (CollectionUtils.isEmpty(msg)||msg.size()>1){
            return;
        }
        for (Map.Entry<Long, List<Long>> entry : msg.entrySet()) {
            Long userId = entry.getKey();
            List<Long> skuIds = entry.getValue();
            cartService.deleteCarts(userId,skuIds);
        }
    }
}
