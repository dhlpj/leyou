package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jack
 * @create 2019-01-22 15:02
 */
@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 构建插入到索引库的goods
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        //查询分类
        List<Category> categories = categoryClient.
                queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        String brandName = brand.getName();
        //封装搜索字段all
        String all = spu.getTitle()+ StringUtils.join(categoryNames,",")+brandName;

        Long spuId = spu.getId();
        //查询sku
        List<Sku> skus = goodsClient.queryBySpuId(spuId);
        Set prices = new HashSet();//保存sku的价格
        List<Map<String,Object>> skuMap = new ArrayList<>();//保存sku->json
        for (Sku sku : skus) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            prices.add(sku.getPrice());
            skuMap.add(map);
        }

        //查询规格参数
        List<SpecParam> specParams = specificationClient.queryParamList(null, spu.getCid3(), true);
        //查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>(){});
        //规格参数，key是规格参数的名称，value是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = "";
            //判断是否是通用规格
            if(specParam.getGeneric()){
                if(specParam.getNumeric()){
                    value = chooseSegment(value.toString(),specParam);
                }else{
                    value = genericSpec.get(specParam.getId());
                }
            }else{
                value = specialSpec.get(specParam.getId());
            }
            //存入map
            specs.put(key,value);
        }
        //构造Good
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(all);//搜索字段，包含标题，分类，甚至品牌，规格等
        goods.setPrice(prices);//sku价格
        goods.setSkus(JsonUtils.serialize(skuMap));//sku信息(json)
        goods.setSpecs(specs);//商品规格参数信息
        return goods;
    }

    /**
     * 获取参数所属的段
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索
     * @param searchRequest
     * @return
     */
    public SearchResult search(SearchRequest searchRequest) {
        int page = searchRequest.getPage()-1;//elasticSearch分页是从0页开始
        int size = searchRequest.getSize();
        //分页
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(page,size));
        //搜索条件及过滤条件,搜索的条件为null,那么搜索结果为null,过滤条件为null,那么代表不过滤
        QueryBuilder basicQuery = buildBasicQueryWithFilter(searchRequest);
        queryBuilder.withQuery(basicQuery);
        //过滤字段(查询结果包含的字段)
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称
        //1、聚合商品分类
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //2、聚合品牌
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //获取商品分类聚合结果
        Aggregation categoryAgg = pageInfo.getAggregation(categoryAggName);
        List<Category> categories = getCategoryAggResult(categoryAgg);
        //获取品牌聚合结果
        Aggregation brandAgg = pageInfo.getAggregation(brandAggName);
        List<Brand> brands = getBrandAggResult(brandAgg);
        //聚合商品规格
        List<Map<String,Object>> specs = null;
        //商品分类为1时，才会聚合查询商品规格信息。
        if(categories.size()==1){
            specs = getSpecs(categories.get(0),basicQuery);
        }
        //获取商品信息
        List<Goods> content = pageInfo.getContent();
        int totalPages = pageInfo.getTotalPages();
        long total = pageInfo.getTotalElements();
        return new SearchResult(total,totalPages,content,categories,brands,specs);
    }

    /**
     * 构建基本查询条件
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request){
        //因为需要过滤搜索结果，所以采用boolQuery
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询条件，Operator.AND,表示搜索的结果 需要满足 搜索条件分词后每个词都会匹配
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if((key.equals("cid3")==false)&&(key.equals("brandId")==false)){
                key = "specs." + key + ".keyword";
            }
            filterQueryBuilder.must(QueryBuilders.termQuery(key,value));
        }
        //添加过滤条件,可以是bool，range，match，terms等等
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    /**
     * 获取规格参数聚合后的结果
     * @param category
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getSpecs(Category category, QueryBuilder basicQuery) {
        //查询该分类下的所有规格参数
        List<SpecParam> specParams = specificationClient.queryParamList(null, category.getId(), true);
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //添加聚合
        specParams.forEach(specParam -> {
            String key = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(key)
                    .field("specs."+key+".keyword"));//自动生成的field
        });
        //执行查询
        Map<String, Aggregation> aggs = template
                .query(queryBuilder.build(), SearchResponse::getAggregations).asMap();
        List<Map<String,Object>> specs = new ArrayList<>();
        //解析结果
        specParams.forEach(specParam -> {
            String key = specParam.getName();
            Aggregation agg = aggs.get(key);
            if(agg instanceof UnmappedTerms==false){//指定的规格参数存在聚合的列表中，一般不会出现指定的规格参数不在聚合列表中，因为添加数据时会添加所有的规格参数
                Map<String,Object> spec = new HashMap<>();
                spec.put("k",key);
                StringTerms terms = (StringTerms) agg;
                //获取每个桶的名称(也就是指定的规格参数的范围)
                spec.put("options",terms.getBuckets().stream()
                        .map(StringTerms.Bucket::getKey)//取桶的key
                        .map(Object::toString)
                        .filter(string->StringUtils.isNotBlank(string))//过滤空字符串
                        .collect(Collectors.toList()));
                specs.add(spec);
            }
        });
        return specs;
    }

    /**
     * 获取聚合后的id结果
     * @param aggregation
     * @return
     */
    private List<Long> getBucketKeys(Aggregation aggregation) {
        LongTerms categoryAgg = (LongTerms)aggregation;
        List<LongTerms.Bucket> buckets = categoryAgg.getBuckets();
        //获取到每个桶的categoryId
        return buckets.stream().map(bucket -> {
            Number keyAsNumber = bucket.getKeyAsNumber();
            return keyAsNumber.longValue();
        }).collect(Collectors.toList());
    }

    /**
     * 解析商品分类聚合结果
     * @param aggregation
     * @return
     */
    private List<Category> getCategoryAggResult(Aggregation aggregation) {
        List<Long> cids = getBucketKeys( aggregation);
        List<Category> categories = categoryClient.queryCategoryByIds(cids);
        return categories;
    }


    /**
     * 解析品牌聚合结果
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        List<Long> brandIds = getBucketKeys(aggregation);
        List<Brand> brands = brandClient.queryBrandByIds(brandIds);
        return brands;
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
