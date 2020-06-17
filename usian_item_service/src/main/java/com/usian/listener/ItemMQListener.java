package com.usian.listener;

import com.usian.service.ItemService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemMQListener {

    @Autowired
    private ItemService itemService;

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = "order_queue",durable = "true"),
            exchange =@Exchange(value="order_exchange",type = ExchangeTypes.TOPIC),
            key = {"order.*"}
    ))
    public void listen(String orderId){
        Integer result = itemService.updateTbItemByOrderId(orderId);
        if(!(result>0)){
            new RuntimeException("扣减失败");
        }
    }
}
