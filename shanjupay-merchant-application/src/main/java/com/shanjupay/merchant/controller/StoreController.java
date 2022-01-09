package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.QRCodeUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Api(value = "商户平台‐门店管理", tags = "商户平台‐门店管理", description = "商户平台‐门店的增删改查")
@RestController
@Slf4j
public class StoreController {

    @org.apache.dubbo.config.annotation.Reference
    private MerchantService merchantService;
    @Reference
    private TransactionService transactionService;
    //门店二维码订单标题
    @Value("${shanjupay.c2b.subject}")
    private String subject;
    //门店二维码订单内容
    @Value("${shanjupay.c2b.body}")
    private String body;

    @ApiOperation("分页条件查询商户下门店")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "页码", name = "pageNo", required = true, dataType = "Integer", paramType = "query"),
        @ApiImplicitParam(value = "页面容量", name = "pageSize", required = true, dataType = "Integer", paramType = "query")
    })
    @PostMapping("/my/stores/merchants/page")
    public PageVO<StoreDTO> queryStoreByPage(@RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize){
        //商户ID
        Long merchantId = SecurityUtil.getMerchantId();
        //查询条件
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(merchantId); //商户id
        //调用service查询分页数据
        return merchantService.queryStoreByPage(storeDTO,pageNo,pageSize);
    }

    @ApiOperation("生成商户应用门店二维码")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用ID", name = "appId", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(value = "门店ID", name = "storeId", required = true, dataType = "Long", paramType = "path")
    })
    @GetMapping("/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable("appId") String appId,
                                          @PathVariable("storeId") Long storeId) throws BusinessException {
        //商户ID
        Long merchantId = SecurityUtil.getMerchantId();
        //生成二维码连接
        QRCodeDTO qrCodeDTO = new QRCodeDTO();
        qrCodeDTO.setMerchantId(merchantId);
        qrCodeDTO.setAppId(appId);
        qrCodeDTO.setStoreId(storeId);
        //标题
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        //用商户名称替换 %s 商品
        qrCodeDTO.setSubject(String.format(subject,merchantDTO.getMerchantName()));
        //用商户名称替换 %s 表示向某商户付款
        qrCodeDTO.setBody(String.format(body,merchantDTO.getMerchantName()));

        String storeQRCodeUrl = transactionService.createStoreQRCode(qrCodeDTO);
        log.info("[merchantId:{},appId:{},storeId:{}]createCScanBStoreQRCode is{}",
                merchantId,appId,storeId,storeQRCodeUrl);
        try{
            QRCodeUtil qrCodeUtil = new QRCodeUtil();
            return qrCodeUtil.createQRCode(storeQRCodeUrl,200,200);
        }catch (IOException e){
            throw  new BusinessException(CommonErrorCode.E_200007);
        }
    }
}
