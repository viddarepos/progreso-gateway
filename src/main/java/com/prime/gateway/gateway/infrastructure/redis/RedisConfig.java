package com.prime.gateway.gateway.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;



@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String REDIS_HOST;
    @Value("${spring.data.redis.port}")
    private Integer REDIS_PORT;

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new GenericToStringSerializer<>(String.class));
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(REDIS_HOST,
            REDIS_PORT);

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .build();
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(
            configuration,
            lettuceClientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return redisTemplate;
    }
}
