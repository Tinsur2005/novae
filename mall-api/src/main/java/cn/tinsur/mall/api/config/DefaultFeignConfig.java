package cn.tinsur.mall.api.config;

import cn.tinsur.mall.util.LoginContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class DefaultFeignConfig {

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户（定时任务等无登录上下文的线程中为null，直接跳过）
                if (LoginContext.getLoginInfo() == null) {
                    return;
                }
                Long id = (Long) LoginContext.getLoginInfo().get("id");;
                String name = (String) LoginContext.getLoginInfo().get("name");;
                if(id == null || !StringUtils.hasText(name)) {
                    // 如果为空则直接跳过
                    return;
                }
                // 如果不为空则放入请求头中，传递给下游微服务
                template.header("X-Login-Id", String.valueOf(id));
                template.header("X-Login-Name", name);
            }
        };
    }
}