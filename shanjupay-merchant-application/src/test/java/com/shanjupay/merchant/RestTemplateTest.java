package com.shanjupay.merchant;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RestTemplateTest{

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testGetHtml(){
        //测试使用restTemplate作为Http的客户端，向http服务端发起请求
        String url = "http://www.baidu.com/";
        //向url发送http请求，得到响应结果
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        String body = forEntity.getBody();
        System.out.println(body);
    }

    //向验证码服务发起请求，获取验证码
    //获取验证码接口网址：http://localhost:56085/sailing/generate?effectiveTime=300&name=sms
    @Test
    public void testCheckCode(){
        String url = "http://localhost:56085/sailing/generate?effectiveTime=300&name=sms";
        //请求体
        Map<String,Object> body = new HashMap<>();
        body.put("mobile","123456");
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //指定content-type: application/json
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //请求信息 传入body 传入header
        HttpEntity httpEntity = new HttpEntity(body,httpHeaders);
        //向url发起请求
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        log.info("请求验证码服务，得到响应:{}", JSON.toJSON(exchange));
        Map exchangeBody = exchange.getBody();
        System.out.println(exchangeBody);
        Map result = (Map) exchangeBody.get("result");
        String key = (String) result.get("key");
        System.out.println(key);
    }
}