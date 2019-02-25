package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Jack
 * @create 2019-02-23 21:32
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/pay",produces = MediaType.APPLICATION_XML_VALUE)
    public Map<String,String> payNotification(@RequestBody Map<String,String> map){
        //处理通知
        Map<String, String> result = orderService.handleNotification(map);
        return result;
    }
}
