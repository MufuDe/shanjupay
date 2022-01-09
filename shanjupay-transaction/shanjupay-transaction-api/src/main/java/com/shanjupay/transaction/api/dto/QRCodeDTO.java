package com.shanjupay.transaction.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@ApiModel(value="QRCodeDTO", description="")
public class QRCodeDTO implements Serializable {

    @ApiModelProperty(value = "商户id")
    private Long merchantId;

    @ApiModelProperty(value = "应用id")
    private String appId;

    @ApiModelProperty(value = "门店id")
    private Long storeId;

    @ApiModelProperty(value = "商品标题")
    private String subject;//商品标题

    @ApiModelProperty(value = "商品描述")
    private String body;//订单描述
}
