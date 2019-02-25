package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Jack
 * @create 2019-02-13 16:31
 */
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties smsProperties;

    /**
     * 监听商品的新增及修改消息
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "leyou.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"//指定匹配的routing key（也就是指定队列的binding key）
    ))
    public void listenInsertOrUpdate(Map<String,String> msg){
        if(msg.size()<2){//没有完整保存phone和code
            return;
        }
        String phone = msg.remove("phone");//获取到值后删除该键值对
        if (StringUtils.isBlank(phone)) {
            return;
        }
        //处理消息
        smsUtils.sendSms(phone,JsonUtils.serialize(msg),smsProperties.getSignName(),smsProperties.getVerifyCodeTemplate());
    }

}
