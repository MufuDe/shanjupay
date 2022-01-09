package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "TP_PAYMENT_ORDER", consumerGroup = "CID_PAYMENT_CONSUMER")
@Slf4j
public class PayConsumer implements RocketMQListener<MessageExt> {

    @org.apache.dubbo.config.annotation.Reference
    PayChannelAgentService payChannelAgentService;
    @Autowired
    PayProducer payProducer;

    @Override
    public void onMessage(MessageExt messageExt) {
        byte[] body = messageExt.getBody();
        String jsonString = new String(body);
        log.info("支付渠道代理服务接收到查询订单的消息:{}", JSON.toJSONString(jsonString));
        //将消息转成对象
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(jsonString,PaymentResponseDTO.class);

        //调用支付宝的订单状态查询接口
        String outTradeNo = paymentResponseDTO.getOutTradeNo();//闪聚平台的订单号
        String jsonParams = String.valueOf(paymentResponseDTO.getContent());
        PaymentResponseDTO responseDTO = null;
        if ("ALIPAY_WAP".equals(paymentResponseDTO.getMsg())){
            //jsonParams将json字符串转为AliConfigParam对象
            AliConfigParam aliConfigParam = JSON.parseObject(jsonParams, AliConfigParam.class);//支付宝支付参数配置
            responseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, outTradeNo);

        }else if ("WX_JSAPI".equals(paymentResponseDTO.getMsg())){
            WXConfigParam wxConfigParam = JSON.parseObject(jsonParams, WXConfigParam.class);//微信支付参数配置
            //调用微信的接口查询订单
            responseDTO = payChannelAgentService.queryPayOrderByWeChat(wxConfigParam, outTradeNo);
        }
        //当没有获取到订单支付结果时，抛出异常，broker再次重试消费
        if (TradeStatus.UNKNOWN.equals(responseDTO.getTradeState())
                || TradeStatus.USERPAYING.equals(responseDTO.getTradeState())
                || responseDTO == null){
            throw new RuntimeException("支付状态未知，等待重试");
        }
        //如果重试的次数达到一定数量，不要再重试消费，将消费记录记录到数据库，由单独的程序或人工进行处理
        //将订单状态，再次发到mq
        payProducer.payResultNotice(responseDTO);
    }
}
