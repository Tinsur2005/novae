package cn.tinsur.mall.config;

import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置：JSON 序列化 + 信任所有包
 * SeckillOrderMessage 在 mall-common（cn.tinsur.mall.pojo.message），
 * 消费端 order-service 的 RabbitMQConfig 必须与本配置一致，否则反序列化失败
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonToMapMessageConverter() {
        DefaultClassMapper defaultClassMapper = new DefaultClassMapper();
        // 指定反序列化期间要信任的一组包，星号 ( * ) 表示全部信任
        defaultClassMapper.setTrustedPackages("*");
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(defaultClassMapper);
        return jackson2JsonMessageConverter;
    }
}
