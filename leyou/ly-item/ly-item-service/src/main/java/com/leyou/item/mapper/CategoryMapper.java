package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author Jack
 * @create 2018-11-27 15:57
 */
public interface CategoryMapper extends Mapper<Category>,IdListMapper<Category,Long> {

}
