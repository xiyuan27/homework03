package com.order.ly.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.order.ly.gateway.handler.SentinelFallbackHandler;
import com.order.ly.license.LicenseCheckListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 网关限流配置
 * 
 * @author LiuYuan
 */
@Configuration
public class GatewayConfig
{
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelFallbackHandler sentinelGatewayExceptionHandler()
    {
        return new SentinelFallbackHandler();
    }

    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter()
    {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置跨域，从配置中读取
     * @return
     */
    @Autowired
    private GlobalCorsProperties globalCorsProperties;
    @Bean
    public CorsWebFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.setCorsConfigurations(globalCorsProperties.getCorsConfigurations());
        return new CorsWebFilter(source);
    }

    @Bean
    public LicenseCheckListener licenseCheckListener() {
        return new LicenseCheckListener();
    }
}