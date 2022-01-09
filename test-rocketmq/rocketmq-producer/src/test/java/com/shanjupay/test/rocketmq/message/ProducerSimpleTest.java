package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerSimpleTest{
    @Autowired
    private ProducerSimple producerSimple;

    /**
     * 同步消息
     */
    @Test
    public void testSendSyncMsg(){
        producerSimple.sendSyncMsg("my-topic","the second Sync msg");
        System.out.println("ending...");
    }

    /**
     * 异步消息
     */
    @Test
    public void testSendASyncMsg(){
        producerSimple.sendASyncMsg("my-topic","the second ASync msg");
        System.out.println("ending...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送对象 同步消息
     */
    @Test
    public void testSendMsgByJson(){
        OrderExt orderExt = new OrderExt();
        orderExt.setId("1");
        orderExt.setMoney(786L);
        orderExt.setCreateTime(new Date());
        producerSimple.sendMsgByJson("my-topic-obj",orderExt);
    }

    @Test
    public void testSendMsgByJsonDelay(){
        OrderExt orderExt = new OrderExt();
        orderExt.setId("2");
        orderExt.setMoney(1234L);
        orderExt.setCreateTime(new Date());
        producerSimple.sendMsgByJsonDelay("my-topic-obj",orderExt);
    }
}