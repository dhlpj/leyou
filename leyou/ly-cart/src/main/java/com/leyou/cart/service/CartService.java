package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jack
 * @create 2019-02-20 10:25
 */
@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String KEY_PREFIX = "cart:user:id:";

    /**
     * 添加商品到购物车
     * 如果用户添加商品到购物车后，商品（sku）进行了修改，再次添加该sku，也不会有误，因为如果sku在后台进行修改，那么会删除原有的sku
     * 添加的是一个新的sku，id不同。
     * @param cart
     */
    public void addCart(Cart cart) {
        //获取线程中的用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX+user.getId();
        //判断redis中是否有该商品
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);//每次操作hashOps的key就是设置好的
        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();
        if (hashOps.hasKey(skuId)) {
            //有，更改数量
            String json = hashOps.get(skuId).toString();
            cart = JsonUtils.parse(json,Cart.class);
            cart.setNum(num+cart.getNum());
        }
        //写回redis
        hashOps.put(skuId,JsonUtils.serialize(cart));
    }

    /**
     * 获取redis中的购物车
     * @return
     */
    public List<Cart> getCartList() {
        //获取登录用户
        BoundHashOperations<String, Object, Object> hashOps = getBoundHashOperations();
        List<Cart> list = hashOps.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class))
                .collect(Collectors.toList());
        return list;

    }

    /**
     * 以userId获取BoundHashOperations
     * @return
     */
    private BoundHashOperations<String, Object, Object> getBoundHashOperations() {
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        return redisTemplate.boundHashOps(key);
    }

    /**
     * 更新cart
     * @param skuId
     * @param num
     */
    public void updateCart(Long skuId, Integer num) {
        //获取登录用户
        BoundHashOperations<String, Object, Object> hashOps = getBoundHashOperations();
        String hashKey = skuId.toString();
        if (!hashOps.hasKey(hashKey)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        Cart cart = JsonUtils.parse(hashOps.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        //写回redis
        hashOps.put(hashKey,JsonUtils.serialize(cart));
    }

    /**
     * 删除cart中指定商品
     * @param skuId
     */
    public void deleteCart(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOps = getBoundHashOperations();
        String hashKey = skuId.toString();
        if (!hashOps.hasKey(hashKey)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //删除
        hashOps.delete(hashKey);
    }


    /**
     * 合并购物车
     * @param carts
     */
    public void mergeCart(Cart[] carts) {
        //判断redis中是否有需要合并的商品，如果没有的话就添加到redis中
        for (Cart cart : carts) {
            addCart(cart);
        }
    }


    /**
     * 删除指定用户的购物车中指定商品
     * @param userId
     * @param skuIds
     */
    public void deleteCarts(Long userId, List<Long> skuIds) {
        String key = KEY_PREFIX + userId;
        if (!redisTemplate.hasKey(key)) {
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        for (Long skuId : skuIds) {
            String hashKey = skuId.toString();
            if (!hashOps.hasKey(hashKey)) {
                throw new LyException(ExceptionEnum.CART_NOT_FOUND);
            }
            //删除
            hashOps.delete(hashKey);
        }
    }
}
