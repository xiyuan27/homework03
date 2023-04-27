package com.order.ly.gateway.filter;

import com.order.ly.common.core.utils.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 黑名单过滤器
 *
 * @author LiuYuan
 */
@Component
public class TokenFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 消息头存在内容，且不存在权限码参数，不处理
        ServerHttpRequest request = exchange.getRequest();
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(StringUtils.isBlank(header)){
            header = request.getQueryParams().getFirst(HttpHeaders.AUTHORIZATION);
            if(StringUtils.isNotBlank(header)){
                //向headers中放文件，记得build
                ServerHttpRequest sq = exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, header).build();
                //将现在的request 变成 exchange对象
                return chain.filter(exchange.mutate().request(sq).build());
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
