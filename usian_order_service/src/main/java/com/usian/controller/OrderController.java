package com.usian.controller;

import com.usian.service.OrderService;
import com.usian.pojo.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/insertOrder")
    public String insertOrder(@RequestBody OrderInfo orderInfo){
        return orderService.insertOrder(orderInfo);
    }
}
