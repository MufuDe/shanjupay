package com.shanjupay.transaction.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支付宝接口对接的测试类
 */
@Slf4j
@Controller
//@RestController//请求方法响应统一为json格式
public class PayTestController {

    String APP_ID = "2021000118659154";//应用ID
    //应用私钥
    String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCujbh5LoDDrIDcO2lmivkrhb5InIIbrOLIoXrQC+zkDTzGtblTRWvocLEZ/LDf3UueAu9ieLF6frQ74+F8000pt40ynEuUWmRn6BVgX1wb4PTE1PCY/62407LT16BVinSF7cPni0JCQVx6U0lu6Q/StxBP9xLNPqqquxTRfXJTTMzaSm63daAen1D3u3+StaWpilXNw/U7pGZR9qt2xE9+/b2LZq48aYWU6E5oB+pQbZp0ReRUtP4V2hl8aTCYJSWPoMwRSzcFizN1Z6uTlMXvvp+Yl4SSzCrBq89/SOtC7X+QroSyJyupwDBU5RCIe299rpaS8WpqMvL/x4yEfuvhAgMBAAECggEAa7DR3Cm9lXtq8PTSnUomWhykfgXZG5v5OpCtxYdl+njUQ0BAe0h8P6avDPpiYWsVSQJEkHsn/AwJxF1ec3WlyJQOz+evlXKDObvs3E1Y9XY/bOR3H922Xy50yM/igoPuSeacE9FN6rSKiomx53Rwp677qGoltmzwPG85c9myXAMiP2eb65eBIzdjiQYAg1jzDKf03WRnwlMzrdpghFFr/9Ui431Q2ThwDfSyR7pcctQhQSht1629mVPoRlJhD1oh0oWlgKStJueEONl2ffSlJFVRZifP8HREe96P9wqIQhMWTtBggHSpO8tDxcSWVMJVR+4HzReDBFzhX4U3NWyGwQKBgQDt/qAqf60JQNmF8rBZYjF+LwkVjf0nC0uUbvVuZS4Cco3gQclEZrb4EAB8fw2SQirodmMa4sWxhoUTKzNE6jfv4zfthZxxmTJCsCnrMue0pAdgTupbmNQJedZjlnT83vrOMPRnZ964WhwA9VX3MsIw2IzlibBh5BfKnkVXn0KUOQKBgQC7wmWrWyx55tEj/j17HHfHr56WcYVrYbuL3ESPAy1WAyd45FXlvPIrasB5igb5X7LOUr39Chn3C/a4VmX2GknRoDs056pAdxZyWqwLGvDBsMAYDhiUPG3m5BgHkI0s2+7xNVcBi/iMolIc8TMGtjrZd3RYGol1TJXc9t00QH4k6QKBgQCPtDPZ9YXyFGpD6yp4jkX8P0Jo9XdqX/OfCPDBW+7QI4Nvacmxt6O7XU4t3jm/mpx2CSYGUe+1JdisnmUys4pbGwPmM8sm/K3QfraRAgfOl0ys5nm0HS/LGPpO38Z7+dfPMNgiqCFfh2lpWroxrUB8ZgWNg/ow4Z1MBw0vv0ZgYQKBgQCUXFpL2YI+XKV7ZGSeR/NZxSpxxAfhvtsb+5Ps0VzcbyecfghlvTQ6HGM0Zl12ZSj7oSPgsz+rFzf/lr2Ahms4Cf1eo4PC5pbFC2KQb9w3oHtZRqIjV9CbtjZKm+0BulOHI1e5sClYrtZlai2gqxO7TImRKH7fQgHVFPfbVu6WKQKBgCS/XAE1umk2QRKOub2uzgxowPVzkLYQrxjXvIPJWenDu76Essl3JjcXnYF+msSnOWDtB2qZmDzmxXpzHgE95g5IP3kJ7SFuUGpt/WVzL8GZipmSMohuRS8Lbp4OHMu3K7FFOtvTvaY/8eVrEIq4/Hy4C91ff9pThiRGjlrLx9Fx";
    //支付宝公钥
    String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh0IwIEwKkD2CNPEm909+h0v2pnGeAJ4hLNmKh9a3ft7Ncjz3hndMJnuRj86Lc+lA0UAZuPKGlmXdxpkUcUynDEZTEbUr2F77GqWLf1YX+1jRI+8b72h9AurLqVbAuQh6VJrdNLQvHxn9V+Inr91wZQUBeErSnJzOIVhD0V/ZkWv+Ey9sWbnEyM+Jvhk2f/53tsrRq4N7xm0NxbxhV/oaAdnY0+2kpvFa19Isl5LKkm8Z1UeVXi2iS2/Q/RwynP5t2N441xYKEwTP2zVz/khuh6rUxnkKg0u8AN9w5qvWcTda4kcHpmuQcUYe44+25X74+k3gw2spRVEgUmQsZKsqawIDAQAB";
    String CHARSET = "utf-8";
    String serverUrl = "https://openapi.alipaydev.com/gateway.do"; //支付宝接口的网关地址，正式为："https://openapi.alipay.com/gateway.do"
    String sign_type = "RSA2";//签名算法类型

    @GetMapping("/alipaytest")
    public void alipaytest(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException {
        //构造sdk客户端类
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do",
                APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2"); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
//        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"20150320010101002\"," +
                " \"total_amount\":\"88.88\"," +
                " \"subject\":\"iPhone8Plus 64G\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form="";
        try {
            //请求支付宝的下单接口，发起请求
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }
}
