package com.leyou.search.client;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jack
 * @create 2019-01-22 13:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataImportTest {
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;

    /**
     * 创建索引库及mapping
     */
    @Test
    public void testGoodsRepository(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    /**
     * 导入数据
     */
    @Test
    public void testImportIndex(){
        int page = 1;
        int rows = 100;
        long count = 0;
        do {
            PageResult<Spu> pageResult = goodsClient.querySpuByPage(page, rows, true, null);
            if(pageResult.getTotal()!=0){
                List<Spu> items = pageResult.getItems();
                //构建goods
                List<Goods> goodsList = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
                //存入索引库
                goodsRepository.saveAll(goodsList);
                count = items.size();
            }
            page++;
        }while (count==rows);

    }

    /**
     * 删除所有
     */
    @Test
    public void testDeleteDocuments(){
        goodsRepository.deleteAll();
    }
}