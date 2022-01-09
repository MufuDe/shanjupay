package com.shanjupay.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.paymentagent.common.constant.AliCodeConstants;
import com.shanjupay.paymentagent.config.WXSDKConfig;
import com.shanjupay.paymentagent.message.PayProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@org.apache.dubbo.config.annotation.Service
@Slf4j
public class PayChannelAgentServiceImpl implements PayChannelAgentService {

    @Autowired
    private PayProducer payProducer;
    /**
     * 调用支付宝手机WAP下单接口
     * @param aliConfigParam 支付渠道参数(配置支付宝的必要参数)
     * @param alipayBean 请求支付参数(业务订单参数)
     * @return 支付返回的信息
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean)
            throws BusinessException {
        String serverUrl = aliConfigParam.getUrl();//支付宝接口网关地址
        String APP_ID = aliConfigParam.getAppId();//支付宝应用ID
        String APP_PRIVATE_KEY = aliConfigParam.getRsaPrivateKey();//支付宝私钥
        String format = aliConfigParam.getFormat();//json格式
        String CHARSET = aliConfigParam.getCharest();//编码类型
        String ALIPAY_PUBLIC_KEY = aliConfigParam.getAlipayPublicKey();//支付宝公钥
        String signType = aliConfigParam.getSigntype();//签名算法
        String returnUrl = aliConfigParam.getReturnUrl();//支付成功回跳地址的url
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付结果异步通知地址的url

        //构造sdk客户端类
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl,
                APP_ID, APP_PRIVATE_KEY, format, CHARSET, ALIPAY_PUBLIC_KEY, signType); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(alipayBean.getOutTradeNo());//闪聚平台订单
        model.setSubject(alipayBean.getSubject());//订单标题
        model.setTotalAmount(alipayBean.getTotalAmount());//订单金额(单位：元)
        model.setBody(alipayBean.getBody());//订单内容
        model.setTimeoutExpress(alipayBean.getExpireTime());//订单过期时间
        model.setProductCode(alipayBean.getProductCode());//商户与支付宝签定的产品码，固定为QUICK_WAP_WAY
        //请求参数集合
        alipayRequest.setBizModel(model);

        String jsonString = JSON.toJSONString(alipayBean);
        log.info("createPayOrderByAliWAP..alipayRequest:{}",jsonString);
        // 设置支付结果异步通知地址url
        alipayRequest.setNotifyUrl(notifyUrl);
        // 设置支付成功跳转同步地址url
        alipayRequest.setReturnUrl(returnUrl);

        try {
            //请求支付宝的下单接口，发起请求
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
            PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
            log.info("调用支付宝下单接口，响应内容:{}",response.getBody());
            paymentResponseDTO.setContent(response.getBody());//支付宝的响应结果

            //向MQ发送一条延迟消息，支付结果查询
            PaymentResponseDTO<AliConfigParam> notice = new PaymentResponseDTO<>();
            notice.setOutTradeNo(alipayBean.getOutTradeNo());//闪聚平台的订单号
            notice.setContent(aliConfigParam);//支付渠道参数
            notice.setMsg("ALIPAY_WAP");//标识是查询支付宝的接口
            //发送消息
            payProducer.payOrderNotice(notice);

            return paymentResponseDTO;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400002);
        }
    }

    /**
     * 支付宝支付状态查询
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo 闪聚平台订单号
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo)
            throws BusinessException{
        String serverUrl = aliConfigParam.getUrl();//支付宝接口网关地址
        String APP_ID = aliConfigParam.getAppId();//支付宝应用ID
        String APP_PRIVATE_KEY = aliConfigParam.getRsaPrivateKey();//支付宝私钥
        String format = aliConfigParam.getFormat();//json格式
        String CHARSET = aliConfigParam.getCharest();//编码类型
        String ALIPAY_PUBLIC_KEY = aliConfigParam.getAlipayPublicKey();//支付宝公钥
        String signType = aliConfigParam.getSigntype();//签名算法
        String returnUrl = aliConfigParam.getReturnUrl();//支付成功回跳地址的url
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付结果异步通知地址的url

        //构造sdk客户端类
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl,
                APP_ID, APP_PRIVATE_KEY, format, CHARSET, ALIPAY_PUBLIC_KEY, signType); //获得初始化的AlipayClient

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);//闪聚平台订单
        //请求参数集合
        request.setBizModel(model);
        AlipayTradeQueryResponse response = null;
        try {
            //请求支付宝订单状态查询接口
            response = alipayClient.execute(request);
            //支付宝响应的code，“10000”表示接口调用成功
            String code = response.getCode();
            if (AliCodeConstants.SUCCESSCODE.equals(code)){
                //解析支付宝返回的状态，解析成闪聚平台的TradeStatus
                String aliTradeStatus = response.getTradeStatus();
                TradeStatus tradeStatus = covertAliTradeStatusToShanjuCode(aliTradeStatus);
                //String tradeNo（支付宝订单号）, String outTradeNo（闪聚平台订单号）, TradeStatus tradeState（订单状态）,
                // String msg（返回信息）
                return PaymentResponseDTO.success(response.getTradeNo(),response.getOutTradeNo(),tradeStatus,
                        response.getMsg());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //String msg, String outTradeNo, TradeStatus tradeState
        return PaymentResponseDTO.fail("支付宝订单查询失败", outTradeNo ,TradeStatus.UNKNOWN);
    }

    /**
     * 微信下单状态查询
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo)
            throws BusinessException {
        WXSDKConfig config = new WXSDKConfig(wxConfigParam);
        Map<String, String> result = null;
        try {
            WXPay wxPay = new WXPay(config);
            Map<String, String> map = new HashMap<>();
            map.put("out_trade_no",outTradeNo);//闪聚平台的订单号
            //调用微信的订单查询接口
            result = wxPay.orderQuery(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String returnCode = result.get("return_code");
        String resultCode = result.get("result_code");
        String tradeState = result.get("trade_state"); //订单状态
        String transactionId = result.get("transaction_id"); //微信订单号
        String tradeType = result.get("trade_type");
        String returnMsg = result.get("return_msg");

        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)){
            if ("SUCCESS".equals(tradeState)) { //交易成功
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.SUCCESS,
                        "");
            } else if ("USERPAYING".equals(tradeState)) { //等待用户支付
                return PaymentResponseDTO.success(transactionId, outTradeNo,
                        TradeStatus.USERPAYING, "");
            } else if ("PAYERROR".equals(tradeState)) { //交易失败
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.FAILED,
                        returnMsg);
            } else if ("CLOSED".equals(tradeState)) { //交易关闭
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.REVOKED,
                        returnMsg);
            }
        }
        return PaymentResponseDTO.success("暂不支持其他状态", transactionId, outTradeNo, TradeStatus.UNKNOWN);
    }

    /**
     * 将支付宝查询时订单状态trade_status 转换为 闪聚订单状态
     * @param aliTradeStatus 支付宝交易状态
     * @return
     */
    private TradeStatus covertAliTradeStatusToShanjuCode(String aliTradeStatus){
        switch (aliTradeStatus){
            case AliCodeConstants.WAIT_BUYER_PAY: //交易创建，等待买家付款
                return TradeStatus.USERPAYING;
            case AliCodeConstants.TRADE_SUCCESS: //交易支付成功
            case AliCodeConstants.TRADE_FINISHED: //交易结束，不可退款
                return TradeStatus.SUCCESS;
            case AliCodeConstants.TRADE_CLOSED: //未付款交易超时关闭，或支付完成后全额退款
                return TradeStatus.REVOKED;
            default:
                return TradeStatus.FAILED;
        }
    }

    @Override
    public Map<String, String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean) {
        //创建sdk客户端
        WXSDKConfig config = new WXSDKConfig(wxConfigParam);
        Map<String,String> jsapiPayParam = null;
        try {
            WXPay wxPay = new WXPay(config);
            //构造请求的参数
            Map<String,String> requestParam = new HashMap<>();
            requestParam.put("out_trade_no",weChatBean.getOutTradeNo());//订单号
            requestParam.put("body", weChatBean.getBody());//订单描述
            requestParam.put("fee_type", "CNY");//人民币
            requestParam.put("total_fee", String.valueOf(weChatBean.getTotalFee())); //金额
            requestParam.put("spbill_create_ip", weChatBean.getSpbillCreateIp());//客户端ip
            requestParam.put("notify_url", weChatBean.getNotifyUrl());//微信异步通知支付结果接口，暂时不用
            requestParam.put("trade_type", "JSAPI");
            //从请求中获取openid
            String openid = weChatBean.getOpenId();
            requestParam.put("openid",openid);
            //调用统一下单接口
            Map<String, String> resp = wxPay.unifiedOrder(requestParam);
            //====向mq写入订单查询的消息====
            PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
            //订单号
            paymentResponseDTO.setOutTradeNo(weChatBean.getOutTradeNo());
            //支付渠道参数
            paymentResponseDTO.setContent(wxConfigParam);
            //msg
            paymentResponseDTO.setMsg("WX_JSAPI");
            payProducer.payOrderNotice(paymentResponseDTO);

            //准备h5网页需要的数据
            jsapiPayParam = new HashMap<>();
            jsapiPayParam.put("appId",wxConfigParam.getAppId());
            jsapiPayParam.put("timeStamp",System.currentTimeMillis()/1000+"");
            jsapiPayParam.put("nonceStr", UUID.randomUUID().toString());//随机字符串
            jsapiPayParam.put("package","prepay_id="+resp.get("prepay_id"));
            jsapiPayParam.put("signType","HMAC-SHA256");
            //将h5网页响应给前端
            jsapiPayParam.put("paySign", WXPayUtil.generateSignature(jsapiPayParam,wxConfigParam.getKey(),
                    WXPayConstants.SignType.HMACSHA256));
            return jsapiPayParam;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400001);
        }
    }
}