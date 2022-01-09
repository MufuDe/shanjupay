package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;

import java.util.Map;

/**
 * 交易订单服务相关接口
 */
public interface TransactionService {

    /**
     * 生成门店二维码
     * @param qrCodeDTO 传入merchantId、appId、storeId、channel、subject、body
     * @return 支付入口URL，将二维码的参数组成json并用base64编码
     * @throws BusinessException
     */
    String createStoreQRCode(QRCodeDTO qrCodeDTO) throws BusinessException;

    /**
     * 支付宝订单保存，1、保存订单到闪聚平台，2、调用支付渠道代理服务调用支付宝的接口
     * @param payOrderDTO 支付订单信息
     * @return 支付响应结果信息
     * @throws BusinessException
     */
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 更新订单支付状态
     * @param tradeNo 闪聚平台订单号
     * @param payChannelTradeNo 支付宝或微信的交易流水号
     * @param state 订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成功,4‐关闭 5‐‐失败
     * @throws BusinessException
     */
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state)
            throws BusinessException;

    /**
     * 获取微信授权码
     * @param payOrderDTO
     * @return 申请授权码的地址
     * @throws BusinessException
     */
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 申请openid
     * @param code 微信授权码
     * @param appId 应用id，用于获取微信支付的参数
     * @return
     * @throws BusinessException
     */
    public String getWXOAuthOpenId(String code, String appId) throws BusinessException;

    /**
     * 微信确认支付，调用微信的接口
     * @param payOrderDTO
     * @return 微信下单接口的响应数据
     * @throws BusinessException
     */
    Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException;
}
