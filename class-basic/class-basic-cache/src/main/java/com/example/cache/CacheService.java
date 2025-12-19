package com.example.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class CacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    public Set keys(String pattern){
        Set keys = redisTemplate.keys(pattern);
        return keys;
    }
    public List hvalues(String key){
        return redisTemplate.opsForHash().values(key);
    }

    public void hkeys(String key){
        redisTemplate.opsForHash().keys(key);
    }

    public Object hget(String key,String field){
        return  redisTemplate.opsForHash().get(key, field);

    }

    public void setex(String key,Object value,Long hours){
        redisTemplate.opsForValue().set(key,value,hours, TimeUnit.HOURS);
    }
    public void setex(String key,Object value,Long time,TimeUnit tu){
        redisTemplate.opsForValue().set(key,value,time, tu);
    }
    public Boolean setnx(String key,Object value,Integer seconds){
        Boolean isok=redisTemplate.opsForValue().setIfAbsent(key,value,seconds, TimeUnit.SECONDS);
        return isok;
    }

    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void del(String key){
        redisTemplate.delete(key);
    }

    public Long ttl(String key){
        return redisTemplate.getExpire(key);
    }

    public void hput(String key, Map map){
        redisTemplate.opsForHash().putAll(key,map);
    }

    public void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }
}
