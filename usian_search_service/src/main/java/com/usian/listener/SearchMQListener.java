package com.usian.listener;

import com.usian.service.SearchItemService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SearchMQListener {

    @Autowired
    private SearchItemService searchItemService;

    /**
     * 监听者接收消息三要素：
     *  1、queue
     *  2、exchange
     *  3、routing key
     */
    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value="search_queue",durable = "true"),
            exchange = @Exchange(value = "item_exchage",type = ExchangeTypes.TOPIC),
            key = {"item.*"}

    ))
    public void listen(String msg) throws IOException {
        Integer insertDoDocument = searchItemService.insertDocument(msg);
        if(insertDoDocument>0){
            throw new RuntimeException("同步失败");
        }
    }
}
