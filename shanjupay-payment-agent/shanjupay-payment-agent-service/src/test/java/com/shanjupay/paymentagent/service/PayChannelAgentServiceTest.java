package com.shanjupay.paymentagent.service;

import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PayChannelAgentServiceTest{

    @org.apache.dubbo.config.annotation.Reference
    PayChannelAgentService payChannelAgentService;

    @Test
    public void testQueryPayOrderByAli(){
        String APP_ID = "2021000118659154";//应用ID
        //应用私钥
        String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCujbh5LoDDrIDcO2lmivkrhb5InIIbrOLIoXrQC+zkDTzGtblTRWvocLEZ/LDf3UueAu9ieLF6frQ74+F8000pt40ynEuUWmRn6BVgX1wb4PTE1PCY/62407LT16BVinSF7cPni0JCQVx6U0lu6Q/StxBP9xLNPqqquxTRfXJTTMzaSm63daAen1D3u3+StaWpilXNw/U7pGZR9qt2xE9+/b2LZq48aYWU6E5oB+pQbZp0ReRUtP4V2hl8aTCYJSWPoMwRSzcFizN1Z6uTlMXvvp+Yl4SSzCrBq89/SOtC7X+QroSyJyupwDBU5RCIe299rpaS8WpqMvL/x4yEfuvhAgMBAAECggEAa7DR3Cm9lXtq8PTSnUomWhykfgXZG5v5OpCtxYdl+njUQ0BAe0h8P6avDPpiYWsVSQJEkHsn/AwJxF1ec3WlyJQOz+evlXKDObvs3E1Y9XY/bOR3H922Xy50yM/igoPuSeacE9FN6rSKiomx53Rwp677qGoltmzwPG85c9myXAMiP2eb65eBIzdjiQYAg1jzDKf03WRnwlMzrdpghFFr/9Ui431Q2ThwDfSyR7pcctQhQSht1629mVPoRlJhD1oh0oWlgKStJueEONl2ffSlJFVRZifP8HREe96P9wqIQhMWTtBggHSpO8tDxcSWVMJVR+4HzReDBFzhX4U3NWyGwQKBgQDt/qAqf60JQNmF8rBZYjF+LwkVjf0nC0uUbvVuZS4Cco3gQclEZrb4EAB8fw2SQirodmMa4sWxhoUTKzNE6jfv4zfthZxxmTJCsCnrMue0pAdgTupbmNQJedZjlnT83vrOMPRnZ964WhwA9VX3MsIw2IzlibBh5BfKnkVXn0KUOQKBgQC7wmWrWyx55tEj/j17HHfHr56WcYVrYbuL3ESPAy1WAyd45FXlvPIrasB5igb5X7LOUr39Chn3C/a4VmX2GknRoDs056pAdxZyWqwLGvDBsMAYDhiUPG3m5BgHkI0s2+7xNVcBi/iMolIc8TMGtjrZd3RYGol1TJXc9t00QH4k6QKBgQCPtDPZ9YXyFGpD6yp4jkX8P0Jo9XdqX/OfCPDBW+7QI4Nvacmxt6O7XU4t3jm/mpx2CSYGUe+1JdisnmUys4pbGwPmM8sm/K3QfraRAgfOl0ys5nm0HS/LGPpO38Z7+dfPMNgiqCFfh2lpWroxrUB8ZgWNg/ow4Z1MBw0vv0ZgYQKBgQCUXFpL2YI+XKV7ZGSeR/NZxSpxxAfhvtsb+5Ps0VzcbyecfghlvTQ6HGM0Zl12ZSj7oSPgsz+rFzf/lr2Ahms4Cf1eo4PC5pbFC2KQb9w3oHtZRqIjV9CbtjZKm+0BulOHI1e5sClYrtZlai2gqxO7TImRKH7fQgHVFPfbVu6WKQKBgCS/XAE1umk2QRKOub2uzgxowPVzkLYQrxjXvIPJWenDu76Essl3JjcXnYF+msSnOWDtB2qZmDzmxXpzHgE95g5IP3kJ7SFuUGpt/WVzL8GZipmSMohuRS8Lbp4OHMu3K7FFOtvTvaY/8eVrEIq4/Hy4C91ff9pThiRGjlrLx9Fx";
        //支付宝公钥
        String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh0IwIEwKkD2CNPEm909+h0v2pnGeAJ4hLNmKh9a3ft7Ncjz3hndMJnuRj86Lc+lA0UAZuPKGlmXdxpkUcUynDEZTEbUr2F77GqWLf1YX+1jRI+8b72h9AurLqVbAuQh6VJrdNLQvHxn9V+Inr91wZQUBeErSnJzOIVhD0V/ZkWv+Ey9sWbnEyM+Jvhk2f/53tsrRq4N7xm0NxbxhV/oaAdnY0+2kpvFa19Isl5LKkm8Z1UeVXi2iS2/Q/RwynP5t2N441xYKEwTP2zVz/khuh6rUxnkKg0u8AN9w5qvWcTda4kcHpmuQcUYe44+25X74+k3gw2spRVEgUmQsZKsqawIDAQAB";
        String CHARSET = "utf-8";
        String serverUrl = "https://openapi.alipaydev.com/gateway.do"; //支付宝接口的网关地址，正式为："https://openapi.alipay.com/gateway.do"
        AliConfigParam aliConfigParam = new AliConfigParam();
        aliConfigParam.setUrl(serverUrl);
        aliConfigParam.setCharest(CHARSET);
        aliConfigParam.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        aliConfigParam.setRsaPrivateKey(APP_PRIVATE_KEY);
        aliConfigParam.setAppId(APP_ID);
        aliConfigParam.setFormat("json");
        aliConfigParam.setSigntype("RSA2");
        //AliConfigParam aliConfigParam, String outTradeNo
        PaymentResponseDTO paymentResponseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam,
                "SJ1466343784915873792");
        System.out.println(paymentResponseDTO);
    }
}