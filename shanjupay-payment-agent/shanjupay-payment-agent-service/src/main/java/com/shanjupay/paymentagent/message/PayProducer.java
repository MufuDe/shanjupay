package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PayProducer {

    //订单结果查询的topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    //发送消息（查询支付宝订单状态）
    public void payOrderNotice(PaymentResponseDTO paymentResponseDTO){
        //发送延迟消息
        Message<PaymentResponseDTO> msg = MessageBuilder.withPayload(paymentResponseDTO).build();
        //延迟第3级发送，延迟10s发送
        rocketMQTemplate.syncSend(TOPIC_ORDER,msg,1000,3);
        log.info("支付渠道代理服务向mq发送订单查询的消息:{}", JSON.toJSONString(paymentResponseDTO));
    }

    //订单结果的topic
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";

    //发送消息（支付结果）
    public void payResultNotice(PaymentResponseDTO paymentResponseDTO){
        rocketMQTemplate.syncSend(TOPIC_RESULT, paymentResponseDTO);
        log.info("支付渠道代理服务向mq发送订单结果的消息:{}", JSON.toJSONString(paymentResponseDTO));
    }
}
