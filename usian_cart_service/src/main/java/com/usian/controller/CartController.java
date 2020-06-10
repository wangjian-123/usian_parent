package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 从redis获取购物车
     * @param userId
     * @return
     */
    @RequestMapping("/getCartFromRedis")
    public Map<String, TbItem> getCartFromRedis(String userId){
        return cartService.getCartFromRedis(userId);
    }

    /**
     * 把购物车存放到Redis中
     * @param userId
     * @param cart
     * @return
     */
    @RequestMapping("/insertCart")
    public Boolean insertCart(String userId, @RequestBody Map<String, TbItem> cart){
        return cartService.insertCart(userId,cart);
    }
}
