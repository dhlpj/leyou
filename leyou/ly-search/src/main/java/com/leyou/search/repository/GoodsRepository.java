package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Jack
 * @create 2019-01-22 13:27
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
