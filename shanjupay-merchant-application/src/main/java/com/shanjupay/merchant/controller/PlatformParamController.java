package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商户平台-渠道和支付参数相关", tags = "商户平台-渠道和支付参数", description = "商户平台-渠道和支付参数相关")
@Slf4j
@RestController
public class PlatformParamController {

    @org.apache.dubbo.config.annotation.Reference
    private PayChannelService payChannelService;

    @ApiOperation("获取平台服务类型")
    @GetMapping(value = "/my/platform-channels")
    public List<PlatformChannelDTO> queryPlatformChannel() {
        return payChannelService.queryPlatformChannel();
    }

    @ApiOperation("绑定服务类型")
    @PostMapping(value = "/my/apps/{appId}/platform-channels")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id", name = "appId", dataType = "string", paramType =
                    "path"),
            @ApiImplicitParam(value = "服务类型code", name = "platformChannelCodes", dataType =
                    "string", paramType = "query")
    })
    public void bindPlatformForApp(@PathVariable("appId") String appId,
                                   @RequestParam("platformChannelCodes") String platformChannelCodes) {
        payChannelService.bindPlatformChannelForApp(appId, platformChannelCodes);
    }

    @ApiOperation("应用绑定服务类型的状态")
    @GetMapping("/my/merchants/apps/platformchannels")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id", name = "appId", dataType = "String", paramType = "query"),
            @ApiImplicitParam(value = "应用id", name = "platformChannel", dataType = "String", paramType = "query")
    })
    public int queryAppBindPlatformChannel(@RequestParam("appId") String appId,
                                           @RequestParam("platformChannel") String platformChannel) {
        return payChannelService.queryAppBindPlatformChannel(appId, platformChannel);
    }

    @ApiOperation("根据服务类型查询支付渠道列表")
    @ApiImplicitParam(value = "服务类型", name = "platformChannelCode", dataType = "String", paramType = "path")
    @GetMapping(value="/my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(
            @PathVariable("platformChannelCode") String platformChannelCode){
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }

    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParam(value = "支付渠道参数", name = "payChannelParam", dataType = "PayChannelParamDTO",
            paramType = "body")
    @RequestMapping(value = "/my/pay-channel-params",
            method = {RequestMethod.POST,RequestMethod.PUT})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParam){
        if (payChannelParam == null || payChannelParam.getChannelName() == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //商户ID
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParam.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParam);
    }

    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用ID", name = "appId", dataType = "String", paramType = "path"),
            @ApiImplicitParam(value = "服务类型", name = "platformChannel", dataType = "String", paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/" +
            "platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable("appId") String appId,
                                                         @PathVariable("platformChannel") String platformChannel){
        return payChannelService.queryPayChannelParamByAppAndPlatform(appId, platformChannel);
    }

    @ApiOperation("获取指定应用指定服务类型下所包含的某个原始支付参数")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用ID", name = "appId", dataType = "String", paramType = "path"),
            @ApiImplicitParam(value = "服务类型", name = "platformChannel", dataType = "String", paramType = "path"),
            @ApiImplicitParam(value = "支付渠道", name = "payChannel", dataType = "String", paramType = "path")
    })
    @GetMapping("/my/pay-channel-params/apps/{appId}/" +
            "platform-channels/{platformChannel}/" +
            "pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable("appId") String appId,
                                                         @PathVariable("platformChannel") String platformChannel,
                                                   @PathVariable("payChannel") String payChannel){
        return payChannelService.queryParamByAppPlatformAndPayChannel(appId, platformChannel, payChannel);
    }
}
