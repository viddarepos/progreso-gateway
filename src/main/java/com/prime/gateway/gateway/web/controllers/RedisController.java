package com.prime.gateway.gateway.web.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.HashMap;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class RedisController {

    @Autowired
    private RedisTemplate redisTemplate;


    public RedisController() {
    }

    @GetMapping("/getAll")
    public HashMap<String, String> getFromRedis() {
        Set<String> redisKeys = redisTemplate.keys("account*");
        assert redisKeys != null;
        HashMap<String, String> hashMap = new HashMap<>();
        for(String key : redisKeys){
            hashMap.put(key, redisTemplate.opsForValue().get(key).toString());
        }
        return hashMap;
    }
}