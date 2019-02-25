package com.example.es_demo.repository;

import com.example.es_demo.pojo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Jack
 * @create 2019-01-18 14:35
 */
public interface ItemRepository extends ElasticsearchRepository<Item,Long> {
}
