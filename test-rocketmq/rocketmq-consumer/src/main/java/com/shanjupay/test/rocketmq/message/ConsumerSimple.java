package com.shanjupay.test.rocketmq.message;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * rocketmq发送消息类
 */
@Component
@RocketMQMessageListener(topic = "my-topic",consumerGroup = "demo-consumer-group")
public class ConsumerSimple implements RocketMQListener<String> {

    //得到消息
    @Override
    public void onMessage(String msg) {
        //此方法被调用，表示接收到消息，msg就是消息内容
        System.out.println(msg);
    }
}