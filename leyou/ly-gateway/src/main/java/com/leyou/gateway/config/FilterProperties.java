package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Jack
 * @create 2019-02-19 14:43
 */
@Data
@ConfigurationProperties("ly.filter")
public class FilterProperties {
    private List<String> allowPaths;
}
