package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 搜索的字段：all,
 * 过滤的字段：id,brandId,cid1,cid2,cid3,createTime,price,specs
 * @author Jack
 * @create 2019-01-21 21:54
 */
@Data
@Document(indexName = "leyou",type = "docs",shards = 1,replicas = 0)
public class Goods {
    @Id
    @Field(type = FieldType.Long)
    private Long id;//spuId

    @Field(type=FieldType.Text, analyzer = "ik_max_word")
    private String all;//所有需要被分词搜索的信息（key），包含标题，分类，甚至品牌，其余的都是过滤信息

    @Field(type = FieldType.Keyword,index = false)
    private String subTitle;//卖点

    //无论是什么类型，index都是true
    //自动创建映射（如果自动创建的属性是字符串，那么会生成两个mapping，分别是*(Text类型)和*.keyword(Keyword类型)）
    private Long brandId;//品牌id
    private Long cid1;//一级分类id
    private Long cid2;//二级分类id
    private Long cid3;//三级分类id
    //自动创建映射,默认将时间转换为long
    private Date createTime;//spu创建时间
    //自动创建映射
    private Set<Long> price;//价格,对应elasticsearch里面的array类型

    @Field(type = FieldType.Keyword,index = false)
    private String skus;//sku信息(图片，价格，名称)的json格式

    //自动创建映射
    //对应elasticsearch中的object类型
    private Map<String,Object> specs;//可搜索的规格参数，key是参数名，值是参数值
    //例如{"机身内存"："4GB"}//会自动生成两个mapping：specs.机身内存和specs.机身内存.keyword 其值都是4GB
}
