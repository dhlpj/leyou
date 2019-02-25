package com.example.es_demo;

import com.example.es_demo.pojo.Item;
import com.example.es_demo.repository.ItemRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsDemoApplicationTests {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testCreate() {
        //创建索引库
        template.createIndex(Item.class);
        //创建mapping
        template.putMapping(Item.class);
    }

    @Test
    public void testInsert(){
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg"));
        list.add(new Item(2L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(3L, "小米手机", " 手机", "小米", 1499.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(4L, "大米手机", " 手机", "大米", 2499.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(5L, "超大米手机", " 手机", "超大米", 3499.00, "http://image.leyou.com/3.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    @Test
    public void testSearchAll(){
        Iterable<Item> items = itemRepository.findAll();
        items.forEach(System.out::println);
    }

    @Test
    public void testNativeSearch(){
        //第一种
        /*Iterable<Item> search = itemRepository.search(QueryBuilders.matchQuery("title", "小米"));
        search.forEach(System.out::println);*/
        //第二种
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("title","小米手机"));
        //过滤条件
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","price"},null));
        //排序条件
        queryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));
        //分页条件
        queryBuilder.withPageable(PageRequest.of(0,2));
        Page<Item> items = itemRepository.search(queryBuilder.build());
        System.out.println("总条数"+items.getTotalElements());
        System.out.println("总页数"+items.getTotalPages());
        items.getContent().forEach(System.out::println);
    }

    /**
     * 聚合
     */
    @Test
    public void testAgg(){
        String name = "popularBrand";
        //不查询任何结果，在所有的索引里面聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(AggregationBuilders.terms(name).field("brand123"));
        //查询并返回聚合
        AggregatedPage<Item> result = template.queryForPage(queryBuilder.build(), Item.class);
        System.out.println(result.getTotalElements());
        Aggregations aggregations = result.getAggregations();
        StringTerms aggregation = aggregations.get(name);//如果该field没有聚合结果，那么返回UnmappedTerms
        List<StringTerms.Bucket> buckets = aggregation.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println("key:"+bucket.getKey()+" docCount:"+bucket.getDocCount());
        });
    }
}

