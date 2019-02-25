package com.leyou.search.client;

import com.leyou.item.pojo.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jack
 * @create 2019-01-22 11:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {
    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void testCategoryClient(){
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(1l, 2l, 3l));
        categories.forEach(System.out::println);
    }


}