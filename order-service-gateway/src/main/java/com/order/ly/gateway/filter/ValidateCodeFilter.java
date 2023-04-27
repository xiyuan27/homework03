package com.order.ly.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.order.ly.common.core.utils.StringUtils;
import com.order.ly.common.core.web.domain.AjaxResult;
import com.order.ly.gateway.service.ValidateCodeService;
import com.order.ly.license.LicenseVerify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 验证码过滤器
 *
 * @author LiuYuan
 */
@Component
public class ValidateCodeFilter extends AbstractGatewayFilterFactory<Object>
{
    private final static String AUTH_URL = "/oauth/token";

    @Autowired
    private ValidateCodeService validateCodeService;

    private static final String BASIC_ = "Basic ";

    private static final String CODE = "code";

    private static final String UUID = "uuid";

    private static final String GRANT_TYPE = "grant_type";

    private static final String LOGIN_TYPE = "login_type";

    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String PASSWORD = "password";

    @Override
    public GatewayFilter apply(Object config)
    {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 非登录请求，不处理
            if (!StringUtils.containsIgnoreCase(request.getURI().getPath(), AUTH_URL))
            {
                return chain.filter(exchange);
            }

            // 刷新token请求，不处理
            String grantType = request.getQueryParams().getFirst(GRANT_TYPE);
            if (StringUtils.containsIgnoreCase(request.getURI().getPath(), AUTH_URL) && StringUtils.containsIgnoreCase(grantType, REFRESH_TOKEN))
            {
                return chain.filter(exchange);
            }

            // 消息头存在内容，且不存在验证码参数，不处理
            String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotEmpty(header) && StringUtils.startsWith(header, BASIC_) && !request.getQueryParams().containsKey(CODE) && !request.getQueryParams().containsKey(UUID))
            {
                return chain.filter(exchange);
            }
            // 获取登录类型
            String loginType = request.getQueryParams().getFirst(LOGIN_TYPE);
            // 非用户名密码登录，不校验验证码
            if (StringUtils.isNotEmpty(loginType)) {
                return chain.filter(exchange);
            }
            // 只有密码模式才验证验证码
            if (PASSWORD.equals(grantType)) {
                try
                {
                    validateCodeService.checkCapcha(request.getQueryParams().getFirst(CODE), request.getQueryParams().getFirst(UUID));
                    // 许可证 先注释
                /*if (!new LicenseVerify().verify()) {
                    throw new IllegalAccessException("您的证书无效，请核查服务器是否取得授权或重新申请证书！");
                }*/
                }
                catch (Exception e)
                {
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                    return exchange.getResponse().writeWith(Mono.just(response.bufferFactory().wrap(JSON.toJSONBytes(AjaxResult.error(e.getMessage())))));
                }
            }

            return chain.filter(exchange);
        };
    }
}
