package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Jack
 * @create 2019-02-01 16:31
 */
@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    /**
     * 监听商品的新增及修改消息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insertOrUpdate.queue",durable = "true"),
            exchange = @Exchange(name = "leyou.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}//指定匹配的routing key（也就是指定队列的binding key）
    ))
    public void listenInsertOrUpdate(Long spuId){
        if(spuId==null){
            return;
        }
        //处理消息，更新或者新增静态页面
        pageService.createHtml(spuId);
    }

    /**
     * 监听商品的修改消息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "leyou.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}//指定匹配的routing key（也就是指定队列的binding key）
    ))
    public void listenDelete(Long spuId){
        if(spuId==null){
            return;
        }
        //处理消息，对静态页面进行删除
        pageService.deleteHtml(spuId);
    }
}
