package com.leyou.order.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Jack
 * @create 2019-02-22 15:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PayHelperTest {
    @Autowired
    private PayHelper payHelper;

    @Test
    public void test(){
        payHelper.createOrder(123121230989766561l,1l,"乐优");
    }
}