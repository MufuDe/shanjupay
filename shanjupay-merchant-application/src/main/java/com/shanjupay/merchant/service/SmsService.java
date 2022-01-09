package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

/**
 * 手机短信验证服务
 */
public interface SmsService{
    /**
     * 获取短信验证码
     * @param phone
     * @return
     */
    String sendMsg(String phone);

    /**
     * 校验手机验证码
     * @param verifyKey
     * @param verifyCode
     */
    void checkVerifyCode(String verifyKey, String verifyCode) throws BusinessException;
}