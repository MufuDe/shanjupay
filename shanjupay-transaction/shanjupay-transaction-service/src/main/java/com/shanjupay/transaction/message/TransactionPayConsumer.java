package com.shanjupay.transaction.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.transaction.api.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_RESULT", consumerGroup = "CID_ORDER_CONSUMER")
public class TransactionPayConsumer implements RocketMQListener<MessageExt>{

    @Reference
    TransactionService transactionService;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonStringBody = new String(body);
        log.info("交易服务接收到支付结果的消息:{}", JSON.toJSONString(jsonStringBody));
        //接收到的消息，内容包括订单状态
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(jsonStringBody, PaymentResponseDTO.class);
        String payChannelTradeNo = paymentResponseDTO.getTradeNo();//支付宝微信的订单号
        String tradeNo = paymentResponseDTO.getOutTradeNo();//闪聚平台的订单号
        //订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成功,4‐关闭 5‐失败
        TradeStatus tradeState = paymentResponseDTO.getTradeState();
        switch (tradeState){
            case SUCCESS:
                //2‐支付成功 String tradeNo, String payChannelTradeNo, String state
                transactionService.updateOrderTradeNoAndTradeState(tradeNo,payChannelTradeNo,"2");
                return;
            case REVOKED:
                //4‐关闭
                transactionService.updateOrderTradeNoAndTradeState(tradeNo,payChannelTradeNo,"4");
                return;
            case FAILED:
                //5‐失败
                transactionService.updateOrderTradeNoAndTradeState(tradeNo,payChannelTradeNo,"5");
                return;
            default:
                throw new RuntimeException(String.format("无法解析支付结果:%s",jsonStringBody));
        }
    }
}