package com.usian.quartz;

import com.usian.mapper.LocalMessageMapper;
import com.usian.mq.MqSender;
import com.usian.pojo.LocalMessage;
import com.usian.pojo.LocalMessageExample;
import com.usian.pojo.TbOrder;
import com.usian.redis.RedisClient;
import com.usian.service.LocalMessageService;
import com.usian.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.java2d.pipe.OutlineTextRenderer;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;

public class OrderQuartz implements Job {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private LocalMessageService localMessageService;

    @Autowired
    private MqSender mqSender;

    /**
     * 关闭超时订单
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(redisClient.setnx("SETNX_LOCK_ORDER_KEY",ip,30L)){
            System.out.println(
                    "===============执行关闭超时订单"+new Date());
            //查询超时订单
            List<TbOrder> tbOrderList = orderService.selectOvertimeOrder();

            //关闭超时订单：status、updatetime、endtime、closetime
            for (TbOrder tbOrder : tbOrderList) {
                orderService.updateOrder(tbOrder);

                //把订单中商品的库存数量加回去
                orderService.updateItem(tbOrder.getOrderId());
            }
            redisClient.del("SETNX_LOCK_ORDER_KEY");

            System.out.println("执行扫描本地消息表的任务...." + new Date());
            List<LocalMessage> localMessageList =
                    localMessageService.selectLocalMessageByStatus();
            for (LocalMessage localMessage : localMessageList) {
                mqSender.sendMessage(localMessage);
            }

        }else{
            System.out.println(
                    "============机器："+ip+" 占用分布式锁，任务正在执行=======================");
        }

    }
}
