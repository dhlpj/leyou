package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CartDTO;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jack
 * @create 2018-12-28 18:04
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //根据上下架进行过滤
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //搜索字符串过滤
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //根据插入时间降序排列
        String orderByClause = "last_update_time"+" DESC";//不能使用驼峰
        example.setOrderByClause(orderByClause);
        //进行查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询分类的品牌及名称
        loadCateoryAndBrandName(spus);
        PageInfo<Spu> pageInfo = new PageInfo<Spu>(spus);
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    //解析分类的品牌及名称
    private void loadCateoryAndBrandName(List<Spu> spus) {
        for(Spu spu:spus){
            //处理分类的名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //处理商品品牌
            Brand brand = brandService.queryById(spu.getBrandId());
            spu.setBname(brand.getName());
        }
    }

    /**
     * 新增商品
     * @param spu
     */
    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(true);

        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增spuDeatils
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        //新增sku和stock
        saveSkuAndStock(spu);
    }

    /**
     * 新增sku和stock
     * @param spu
     */
    private void saveSkuAndStock(Spu spu) {
        int count;
        List<Sku> skus = spu.getSkus();
        //存放stock
        List<Stock> stocks = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setEnable(true);
            sku.setSpuId(spu.getId());
            //新增
            count = skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //待新增的一些库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
        //批量新增库存
        count = stockMapper.insertList(stocks);
        if(count!=stocks.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //给mq发送消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());//指定匹配的routing key（也就是binding key）
    }

    /**
     * 根据spuId查询对应的SpuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据spuId查询sku信息
     * @param spuId
     * @return
     */
    public List<Sku> queryBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSkus(skus);
        return skus;
    }

    private void loadStockInSkus(List<Sku> skus) {
        //获取sku的id
        List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
        //查询库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stocks)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //封装到skus中
        //1、将stocks转化为map，skuId为key，stock属性为value
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        //2、将stock封装到sku中
        skus.forEach(s->s.setStock(map.get(s.getId())));
    }

    /**
     * 更新商品信息
     * @param spu
     */
    @Transactional
    public void updateGoods(Spu spu) {
        //删除sku和stock
        if(spu.getId()==null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skus = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skus)){
            //删除sku
            skuMapper.delete(sku);
            //查询skus对应的ids
            List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
            //删除stock
            stockMapper.deleteByIdList(skuIds);
        }
        //修改spu
        spu.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        SpuDetail spuDetail = spu.getSpuDetail();
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);
        //给mq发送消息(指定routing key)
        amqpTemplate.convertAndSend("item.update",spu.getId());//在yml文件中指定exchange
    }


    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(queryBySpuId(id));
        //查询detail
        spu.setSpuDetail(querySpuDetailById(id));
        return spu;
    }

    /**
     * 根据ids查询skus
     * @param ids
     * @return
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        loadStockInSkus(skus);
        return skus;
    }

    /**
     * 减去指定商品库存
     * @param carts
     */
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if(count!=1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
