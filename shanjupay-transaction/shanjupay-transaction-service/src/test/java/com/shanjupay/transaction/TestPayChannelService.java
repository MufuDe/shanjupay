package com.shanjupay.transaction;

import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class TestPayChannelService{

    @Autowired
    PayChannelService payChannelService;

    @Test
    public void testQueryPayChannelByPlatformChannel(){
        List<PayChannelDTO> payChannelDTOS = payChannelService.
                queryPayChannelByPlatformChannel("shanju_b2c");
        for (PayChannelDTO payChannelDTO : payChannelDTOS) {
            System.out.println(payChannelDTO);
        }
    }
}