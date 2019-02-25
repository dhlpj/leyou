package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Jack
 * @create 2019-02-21 10:51
 */
@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/order")
    public ResponseEntity<Long> generateOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.generateOrder(orderDTO));
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("/order/{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接
     * @param id
     * @return
     */
    @GetMapping("/order/url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.generateUrl(id));
    }

    @GetMapping("order/state/{id}")
    public ResponseEntity<Integer> queryPayStatus(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.queryPayStatus(id).getState());
    }
}
