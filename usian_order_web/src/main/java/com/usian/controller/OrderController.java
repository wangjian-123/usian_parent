package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/frontend/order")
public class OrderController {


    @Autowired
    private CartServiceFeign cartServiceFeign;

    /**
     * 订单确认页面的查询
     * @param ids
     * @param userId
     * @return
     */
    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids,String userId){
        Map<String, TbItem> cart = cartServiceFeign.getCartFromRedis(userId);
        List<TbItem> list = new ArrayList<>();
        for (String id : ids) {
            list.add(cart.get(id));
        }
        if(list.size()>0){
            return Result.ok(list);
        }
        return Result.error("error");
    }
}
