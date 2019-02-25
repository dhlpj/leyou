package com.leyou.page.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Jack
 * @create 2019-01-30 15:08
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PageServiceTest {
    @Autowired
    private PageService pageService;

    @Test
    public void testCreateHtml(){
        pageService.createHtml(123l);
    }
}