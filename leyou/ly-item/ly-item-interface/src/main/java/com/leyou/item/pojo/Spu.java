package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "tb_spu")
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String title;// 标题
    private String subTitle;// 子标题
    private Boolean saleable;// 是否上架
    private Boolean valid;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间
    private Date lastUpdateTime;// 最后修改时间

    //展示商品时需要额外用到的字段
    @Transient//与数据库不对应的字段
    private String cname;//展示总的类目信息名称
    @Transient
    private String bname;//展示品牌名称

    //添加商品需要额外使用到的字段
    @Transient
    private List<Sku> skus;
    @Transient
    private SpuDetail spuDetail;
}
