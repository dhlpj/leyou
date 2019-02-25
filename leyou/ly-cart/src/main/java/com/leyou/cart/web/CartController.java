package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jack
 * @create 2019-02-20 10:25
 */
@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * 向购物车添加商品
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 获取购物车
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> getCartList(){
        return ResponseEntity.ok(cartService.getCartList());
    }

    /**
     * 对购物车中商品数量进行修改
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCart(@RequestParam("id") Long skuId,@RequestParam("num") Integer num){
        cartService.updateCart(skuId,num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车中指定商品
     * @param skuId
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable("id") Long skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeCart(@RequestBody Cart[] carts){
        cartService.mergeCart(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
