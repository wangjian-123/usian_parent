package com.usian.listener;

import com.rabbitmq.client.Channel;
import com.usian.pojo.DeDuplication;
import com.usian.pojo.LocalMessage;
import com.usian.service.DeduplicationService;
import com.usian.service.ItemService;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Date;

@Configuration
public class ItemMQListener {

    @Autowired
    private ItemService itemService;

    @Autowired
    private DeduplicationService deduplicationService;

    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = "order_queue",durable = "true"),
            exchange =@Exchange(value="order_exchange",type = ExchangeTypes.TOPIC),
            key = {"order.*"}
    ))
    public void listen(String msg, Channel channel, Message message) throws IOException {
        LocalMessage localMessage = JsonUtils.jsonToPojo(msg, LocalMessage.class);
        //查询消息记录表
        DeDuplication deDuplication = deduplicationService.selectDeDuplicationByTxNo(localMessage.getTxNo());
        //不存在，执行事务
        if(deDuplication==null){
            //int a = 6/0;
            Integer result = itemService.updateTbItemByOrderId(localMessage.getOrderNo());
            if(!(result>0)){
                new RuntimeException("扣减失败");
            }
            deduplicationService.insertDeDuplication(localMessage.getTxNo());

        }else {
            System.out.println("=======幂等生效：事务"+deDuplication.getTxNo()
                    +" 已成功执行===========");
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
