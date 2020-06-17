package com.usian.service;

import com.usian.mapper.TbOrderItemMapper;
import com.usian.mapper.TbOrderMapper;
import com.usian.mapper.TbOrderShippingMapper;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderItem;
import com.usian.pojo.TbOrderShipping;

import com.usian.redis.RedisClient;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Value("${ORDER_ID_KEY}")
    private String ORDER_ID_KEY;

    @Value("${ORDER_ID_BEGIN}")
    private Long ORDER_ID_BEGIN;

    @Value("${ORDER_ITEM_ID_KEY}")
    private String ORDER_ITEM_ID_KEY;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public String insertOrder(OrderInfo orderInfo) {
        String orderItem = orderInfo.getOrderItem();
        TbOrder tbOrder = orderInfo.getTbOrder();
        TbOrderShipping tbOrderShipping = orderInfo.getTbOrderShipping();
        List<TbOrderItem> orderItemList = JsonUtils.jsonToList(orderItem, TbOrderItem.class);

        //保存订单信息
        if(!redisClient.exists(ORDER_ID_KEY)){
            redisClient.set(ORDER_ID_KEY,ORDER_ID_BEGIN);
        }
        Long orderId = redisClient.incr(ORDER_ID_KEY, 1L);
        Date date = new Date();
        tbOrder.setCreateTime(date);
        tbOrder.setUpdateTime(date);
        //状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
        tbOrder.setStatus(1);
        tbOrder.setOrderId(orderId.toString());
        tbOrderMapper.insertSelective(tbOrder);

        //保存明细信息
        if(!redisClient.exists(ORDER_ITEM_ID_KEY)){
            redisClient.set(ORDER_ITEM_ID_KEY,0);
        }
        for (int i = 0; i < orderItemList.size(); i++) {
            Long orderItemId = redisClient.incr(ORDER_ITEM_ID_KEY, 1L);
            TbOrderItem tbOrderItem = orderItemList.get(i);
            tbOrderItem.setId(orderItemId.toString());
            tbOrderItem.setOrderId(orderId.toString());
            tbOrderItemMapper.insertSelective(tbOrderItem);
        }

        //保存物流信息
        tbOrderShipping.setOrderId(orderId.toString());
        tbOrderShipping.setCreated(date);
        tbOrderShipping.setUpdated(date );
        tbOrderShippingMapper.insertSelective(tbOrderShipping);

        //发送消息到mq
        amqpTemplate.convertAndSend("order_exchange","order.add",orderId);

        return orderId.toString();
    }
}
