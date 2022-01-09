package com.shanjupay.merchant.service.Impl;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${sms.url}")
    String url;

    @Value("${sms.effectiveTime}")
    String effectiveTime;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public String sendMsg(String phone) {
        //向验证码服务发送请求获取验证码
        String sms_url = url + "/generate?effectiveTime=" + effectiveTime + "&name=sms";
        //请求体
        Map<String,Object> body = new HashMap<>();
        body.put("mobile",phone);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //指定content-type: application/json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //请求信息 传入body 传入header
        HttpEntity httpEntity = new HttpEntity(body,httpHeaders);
        //向url发起请求
        ResponseEntity<Map> exchange = null;
        Map exchangeBody = null;
        try {
            exchange = restTemplate.exchange(sms_url, HttpMethod.POST, httpEntity, Map.class);
            log.info("请求验证码服务，得到响应:{}", JSON.toJSON(exchange));
            exchangeBody = exchange.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("发送验证码失败");
        }
        if (exchangeBody == null || exchangeBody.get("result") == null){
            throw new RuntimeException("发送验证码失败");
        }

        Map result = (Map) exchangeBody.get("result");
        String key = (String) result.get("key");
        log.info("得到发送验证码对应的key:{}",key);
        return key;
    }

    @Override
    public void checkVerifyCode(String verifyKey, String verifyCode) throws BusinessException {
        //定义校验验证码的URL
        //校验验证码url：
        String url = "http://localhost:56085/sailing/verify?name=sms&verificationCode=" + verifyCode
                + "&verificationKey=" + verifyKey;
        //使用restTemlate来请求验证码服务
        Map bodyMap = null;
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            log.info("校验验证码服务，得到响应:{}", JSON.toJSON(exchange));
            bodyMap = exchange.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            //throw new RuntimeException("校验验证码失败");
            throw new BusinessException(CommonErrorCode.E_100102);
        }
        if (bodyMap == null || bodyMap.get("result") == null || !(Boolean)bodyMap.get("result")) {
            //throw new RuntimeException("校验验证码失败");
            throw new BusinessException(CommonErrorCode.E_100102);
        }
    }
}
