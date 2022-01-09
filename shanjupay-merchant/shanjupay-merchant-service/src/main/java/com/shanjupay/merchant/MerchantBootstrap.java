package com.shanjupay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MerchantBootstrap{
    public static void main(String[] args) {
        SpringApplication.run(MerchantBootstrap.class,args);
    }
}