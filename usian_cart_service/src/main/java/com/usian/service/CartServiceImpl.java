package com.usian.service;

import com.usian.pojo.TbItem;
import com.usian.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class CartServiceImpl implements CartService{

    @Autowired
    private RedisClient redisClient;

    @Value("${CART_REDIS_KEY}")
    private String CART_REDIS_KEY;

    /**
     * 从redis获取购物车
     * @param userId
     * @return
     */
    @Override
    public Map<String, TbItem> getCartFromRedis(String userId) {
        return (Map<String, TbItem>) redisClient.hget(CART_REDIS_KEY, userId);
    }

    /**
     * 把购物车存放到Redis中
     * @param userId
     * @param cart
     * @return
     */
    @Override
    public Boolean insertCart(String userId, Map<String, TbItem> cart) {
        return redisClient.hset(CART_REDIS_KEY, userId, cart);
    }
}
