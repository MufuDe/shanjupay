package com.shanjupay.paymentagent;


import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentAgentBootstrap {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RocketMQTemplate rocketMQTemplate(){
        return new RocketMQTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(PaymentAgentBootstrap.class, args);
    }

}
