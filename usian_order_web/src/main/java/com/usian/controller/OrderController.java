package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderShipping;
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
    private OrderServiceFeign orderServiceFeign;

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

    /**
     * 提交订单
     * @param orderItem
     * @param tbOrder
     * @param tbOrderShipping
     * @return
     */
    @RequestMapping("/insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping){
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderItem(orderItem);
        orderInfo.setTbOrder(tbOrder);
        orderInfo.setTbOrderShipping(tbOrderShipping);
        String orderId = orderServiceFeign.insertOrder(orderInfo);
        if(orderId!=null){
            return Result.ok(orderId);
        }
            return Result.error("订单提交失败");
    }
}
