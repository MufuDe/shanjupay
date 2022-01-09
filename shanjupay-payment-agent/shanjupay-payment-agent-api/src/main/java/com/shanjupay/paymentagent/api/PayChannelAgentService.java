package com.shanjupay.paymentagent.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;

import java.util.Map;

/**
 * 与第三方支付渠道进行交互
 */
public interface PayChannelAgentService {

    /**
     * 调用支付宝手机WAP下单接口
     * @param aliConfigParam 支付渠道参数(配置支付宝的必要参数)
     * @param alipayBean 请求支付参数(业务订单参数)
     * @return 支付返回的信息
     * @throws BusinessException
     */
    PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean)
            throws BusinessException;

    /**
     * 支付宝订单交易状态查询
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo 闪聚平台订单号
     * @return
     */
    PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException;

    /**
     * 查询微信的订单状态
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     * @throws BusinessException
     */
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo)
            throws BusinessException;

    /**
     * 微信jsapi下单接口请求
     * @param wxConfigParam 微信支付渠道的参数
     * @param weChatBean 订单参数
     * @return h5页面所需要的数据
     */
    public Map<String,String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean)
            throws BusinessException;

}
