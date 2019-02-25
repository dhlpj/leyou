package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Jack
 * @create 2019-01-29 16:17
 */
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${item.html.path}")
    private String path;

    /**
     * 加载页面数据
     * @param spuId
     * @return
     */
    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> map = new HashMap<>();
        Spu spu = goodsClient.querySpuById(spuId);
        map.put("title",spu.getTitle());
        map.put("subTitle",spu.getSubTitle());
        map.put("detail",spu.getSpuDetail());
        map.put("skus",spu.getSkus());
        List<Category> categories = categoryClient
                .queryCategoryByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        map.put("categories",categories);
        map.put("brand",brandClient.queryBrandById(spu.getBrandId()));
        map.put("specs",specificationClient.queryGroupsAndParamsByCid(spu.getCid3()));
        return map;
    }

    public void createHtml(Long spuId){
        PrintWriter writer = null;
        try {
            Context context = new Context();
            Map<String, Object> map = loadModel(spuId);
            context.setVariables(map);
            File file = new File(path, spuId + ".html");
            if (file.exists()){
                file.delete();
            }
            writer = new PrintWriter(file);
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            log.error("【生成HTML静态文件出错】",e);
        }finally {
            if (writer!=null){
                writer.close();
            }
        }

    }

    /**
     * 删除静态页面
     * @param spuId
     */
    public void deleteHtml(Long spuId) {
        File file = new File(path, spuId + ".html");
        if(file.exists()){
            file.delete();
        }
    }
}
