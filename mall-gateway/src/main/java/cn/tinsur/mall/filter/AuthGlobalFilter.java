package cn.tinsur.mall.filter;

import cn.tinsur.mall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        //不需要进行验证的路径
        if (path.startsWith("/service/captcha")
                || path.startsWith("/admin/login")
                || path.startsWith("/user/login")) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        //验证token
        try {
            Map<String, Object> map = JwtUtil.parseToken(token);
            // 继续往下游传递用户信息
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-Login-Id", String.valueOf(map.get("id")))
                    .header("X-Login-Name", String.valueOf(map.get("name")))
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            //http的响应状态改成401
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}