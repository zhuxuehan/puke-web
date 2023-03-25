package com.zzz.puke.service;

import com.zzz.puke.aspect.InterfaceCountAspect;
import com.zzz.puke.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;

@Service
public class MethodService {

    @Autowired
    RedisTemplate redisTemplate;

    public void sendCountAndClear() {
        Set<String> keys = redisTemplate.keys(InterfaceCountAspect.METHOD_COUNT + "*");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashMap<String, Object> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, valueOperations.get(key));
        }
        WechatUtils.sendErrorMessage(result.toString());
        redisTemplate.delete(keys);
    }
}
