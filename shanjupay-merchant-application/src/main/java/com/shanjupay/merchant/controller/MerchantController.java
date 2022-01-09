package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.*;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Api(value = "商户平台‐商户管理", tags = "商户平台‐商户相关", description = "商户平台‐商户相关")
public class MerchantController{

    @org.apache.dubbo.config.annotation.Reference //注入远程调用的接口
    private MerchantService merchantService;

    @Autowired //将本地的bean注入
    SmsService smsService;

    @Autowired
    FileService fileService;

    @ApiOperation("根据商户ID查询商户")
    @ApiImplicitParam(name = "id", value = "商户id", dataType = "Long", paramType = "path")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id){
        return merchantService.queryMerchantById(id);
    }

    @ApiOperation("获取登录用户的商户信息")
    @GetMapping(value="/my/merchants")
    public MerchantDTO getMyMerchantInfo(){
        Long merchantId = SecurityUtil.getMerchantId();
        return merchantService.queryMerchantById(merchantId);
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String", paramType = "query")
    @GetMapping("/sms")
    public String getSMSCode(@RequestParam("phone") String phone){
        //向验证码服务请求发送验证码
        return smsService.sendMsg(phone);
    }

    @ApiOperation("商户注册")
    @ApiImplicitParam(name = "merchantRegisterVO", value = "注册信息", required = true, dataType =
            "MerchantRegisterVO", paramType = "body")
    @PostMapping("/merchants/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegisterVO){

        //校验参数的合法性
        if (merchantRegisterVO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //校验手机号是否为空
        if (StringUtils.isEmpty(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //手机号格式校验
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }

        //校验验证码
        smsService.checkVerifyCode(merchantRegisterVO.getVerifiykey(),merchantRegisterVO.getVerifiyCode());
        //调用Dubbo服务的接口
        //MerchantDTO merchantDTO = new MerchantDTO();
        //向DTO写入商户注册的信息
        //merchantDTO.setMobile(merchantRegisterVO.getMobile());
        //merchantDTO.setUsername(merchantRegisterVO.getUsername());
        //使用MapStruct转换对象
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @ApiOperation("证件上传")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件", required = true) @RequestParam("file")
                                     MultipartFile multipartFile) throws IOException {
        //调用fileService进行文件上传
        //生成的文件名名称fileName，要保证它的唯一
        //文件原始名称
        String originalFilename = multipartFile.getOriginalFilename();
        //扩展名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") - 1);
        //文件名称
        String fileName = UUID.randomUUID() + suffix;
        return fileService.upload(multipartFile.getBytes(), fileName);
    }

    @ApiOperation("商户资质申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantInfo", value = "商户认证资料", required = true,
                    dataType = "MerchantDetailVO", paramType = "body")
    })
    @PostMapping("/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantInfo){
        //解析token，取出当前登录的商户的ID
        Long merchantId = SecurityUtil.getMerchantId();
        System.out.println(merchantId);

        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantInfo);
        merchantService.applyMerchant(merchantId,merchantDTO);
    }
}