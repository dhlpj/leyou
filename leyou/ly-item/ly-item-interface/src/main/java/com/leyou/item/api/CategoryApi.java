package com.leyou.item.api;

import com.leyou.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Jack
 * @create 2019-01-22 13:09
 */
@RequestMapping("/category")
public interface CategoryApi {
    @GetMapping("/list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids")List<Long> ids);
}
