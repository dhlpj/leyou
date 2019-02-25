package com.leyou.item.mapper;

import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author Jack
 * @create 2019-01-10 22:39
 */
public interface StockMapper extends Mapper<Stock>, InsertListMapper<Stock>, IdListMapper<Stock,Long> {
    @Update("UPDATE tb_stock SET stock = stock - #{num} WHERE sku_id = #{skuId} and stock>=#{num}")
    int decreaseStock(@Param("skuId") Long skuId,@Param("num") Integer num);
}
