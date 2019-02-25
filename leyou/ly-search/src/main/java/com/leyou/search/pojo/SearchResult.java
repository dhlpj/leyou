package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Jack
 * @create 2019-01-24 10:58
 */
@Data
public class SearchResult extends PageResult<Goods> {
    private List<Category> categories;//商品分类
    private List<Brand> brands;//商品品牌
    private List<Map<String,Object>> specs;
    public SearchResult(Long total, Integer totalPage, List<Goods> items,
                        List<Category> categories, List<Brand> brands,List<Map<String,Object>> specs){
        super(total,totalPage,items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
