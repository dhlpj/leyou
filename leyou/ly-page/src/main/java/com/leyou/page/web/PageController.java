package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Jack
 * @create 2019-01-28 17:27
 */
@Controller
@RequestMapping("/item")
public class PageController {
    @Autowired
    private PageService pageService;

    @RequestMapping("/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model){
        Map<String,Object> attributes = pageService.loadModel(spuId);
        model.addAllAttributes(attributes);
        return "item";
    }
}
