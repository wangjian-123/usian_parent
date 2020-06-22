package com.usian.mq;

import com.usian.mapper.LocalMessageMapper;
import com.usian.pojo.LocalMessage;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调
 * ReturnCallback接口用于实现消息发送到RabbitMQ交换器，但无相应队列与交换器绑定时的回调
 */
public class MqSender implements ReturnCallback, ConfirmCallback {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private LocalMessageMapper localMessageMapper;

    /**
     *下游服务消息确认成功返回后调用
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        // 如果发送到交换器都没有成功（比如说删除了交换器），ack 返回值为 false
        // 如果发送到交换器成功，但是没有匹配的队列（比如说取消了绑定），ack 返回值为还是 true （这是一个坑，需要注意）
        if(ack){
            String txNo = correlationData.getId();
            LocalMessage localMessage = new LocalMessage();
            localMessage.setTxNo(txNo);
            localMessage.setState(1);
            localMessageMapper.updateByPrimaryKeySelective(localMessage);
        }
    }

    /**
     * 消息发送失败调用
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("return--message:" + new String(message.getBody())
                + ",exchange:" + exchange + ",routingKey:" + routingKey);
    }

    /**
     * 发送消息
     * @param localMessage
     */
    public void sendMessage(LocalMessage localMessage){
        RabbitTemplate rabbitTemplate = (RabbitTemplate) this.amqpTemplate;

        rabbitTemplate.setReturnCallback(this);
        // 如果发送到交换器成功，但是没有匹配的队列，就会触发这个回调
        rabbitTemplate.setConfirmCallback(this);
        //消息确认对象：消息id:用户确认成功返回后修改本地消息表状态
        CorrelationData correlationData = new CorrelationData(localMessage.getTxNo());


        //发送消息到mq
        rabbitTemplate.convertAndSend("order_exchange","order.add",
                JsonUtils.objectToJson(localMessage),correlationData);
    }
}
