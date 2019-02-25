package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author Jack
 * @create 2019-01-10 22:38
 */
public interface SkuMapper extends Mapper<Sku>, IdListMapper<Sku,Long> {
}
