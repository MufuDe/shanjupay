package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QRCodeDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 *
 */
@org.apache.dubbo.config.annotation.Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    //从配置文件读取支付入口地址
    @Value("${shanjupay.payurl}")
    private String payurl;
    @Value("${weixin.oauth2RequestUrl}")
    private String wxOAuth2RequestUrl;
    @Value("${weixin.oauth2CodeReturnUrl}")
    private String wxOAuth2CodeReturnUrl;
    @Value("${weixin.oauth2Token}")
    private String oauth2Token;
    @Reference
    private AppService appService;
    @Reference
    private MerchantService merchantService;
    @Autowired
    private PayOrderMapper payOrderMapper;
    @Reference
    private PayChannelAgentService payChannelAgentService;
    @Autowired
    private PayChannelService payChannelService;

    @Override
    public String createStoreQRCode(QRCodeDTO qrCodeDTO) throws BusinessException {
        //校验应用和门店是否属于当前登录商户
        verifyAppAndStore(qrCodeDTO.getMerchantId(), qrCodeDTO.getAppId(), qrCodeDTO.getStoreId());
        //组装url所需的数据
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDTO.getMerchantId());
        payOrderDTO.setAppId(qrCodeDTO.getAppId());
        payOrderDTO.setStoreId(qrCodeDTO.getStoreId());
        payOrderDTO.setSubject(qrCodeDTO.getSubject());//显示订单标题
        payOrderDTO.setChannel("shanju_c2b");//服务类型，要写为C扫B的服务类型
        payOrderDTO.setBody(qrCodeDTO.getBody());//订单内容
        //转成JSON
        String jsonString = JSON.toJSONString(payOrderDTO);
        //BASE64编码
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        //生成支付入口的url，需要携带参数，将传入的参数转换成json，用base64编码
        String url = payurl + ticket;
        return url;
    }

    /**
     * 校验应用和门店是否属于当前登录商户
     *
     * @param merchantId 商户ID
     * @param appId      应用ID
     * @param storeId    门店ID
     */
    private void verifyAppAndStore(Long merchantId, String appId, Long storeId) {
        //判断应用是否属于当前商户
        Boolean appInMerchant = appService.queryAppInMerchant(appId, merchantId);
        if (!appInMerchant) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        //判断门店是否属于当前商户
        Boolean storeInMerchant = merchantService.queryStoreInMerchant(storeId, merchantId);
        if (!storeInMerchant) {
            throw new BusinessException(CommonErrorCode.E_200006);
        }
    }

    /**
     * 支付宝订单保存，1、保存订单到闪聚平台，2、调用支付渠道代理服务调用支付宝的接口
     *
     * @param payOrderDTO 支付订单信息
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {
        //保存订单到闪聚平台
        payOrderDTO.setChannel("ALIPAY_WAP");//支付渠道
        PayOrderDTO save = save(payOrderDTO);
        //调用支付渠道代理服务调用支付宝的接口
        PaymentResponseDTO paymentResponseDTO = alipayH5(save.getTradeNo());
        return paymentResponseDTO;
    }

    /**
     * 调用支付渠道的代理下单接口：支付宝
     *
     * @param tradeNo
     * @return
     */
    private PaymentResponseDTO alipayH5(String tradeNo) {
        //前端提交的订单信息，从数据库查询
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        //组装alipayBean
        AlipayBean alipayBean = new AlipayBean();
        alipayBean.setOutTradeNo(tradeNo);//订单号
        alipayBean.setSubject(payOrderDTO.getSubject());//标题
        try {
            alipayBean.setTotalAmount(AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString()));//金额
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
        alipayBean.setBody(payOrderDTO.getBody());//订单内容
        alipayBean.setStoreId(payOrderDTO.getStoreId());//门店ID
        alipayBean.setExpireTime("30m");//过期时间
        //支付渠道配置参数，从数据库查询
        //String appId, String platformChannel, String payChannel
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(
                payOrderDTO.getAppId(), "shanju_c2b", "ALIPAY_WAP");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        String paramJson = payChannelParamDTO.getParam();
        //支付渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(paramJson, AliConfigParam.class);
        //字符编码
        aliConfigParam.setCharest("utf-8");
        PaymentResponseDTO payOrderByAliWAP = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);
        return payOrderByAliWAP;
    }

    /**
     * 根据订单号查询订单信息
     *
     * @param tradeNo 闪聚平台的订单号
     * @return 数据库里查询到的订单信息
     */
    public PayOrderDTO queryPayOrder(String tradeNo) {
        PayOrder payOrder = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getTradeNo, tradeNo));
        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }

    /**
     * 保存订单到闪聚平台
     *
     * @param payOrderDTO 订单信息
     * @return
     * @throws BusinessException
     */
    private PayOrderDTO save(PayOrderDTO payOrderDTO) throws BusinessException {
        PayOrder entity = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);
        //获取唯一订单号
        entity.setTradeNo(PaymentUtil.genUniquePayOrderNo());//采用雪花算法生成订单号
        //订单创建时间
        entity.setCreateTime(LocalDateTime.now());
        //设置过期时间，30分钟
        entity.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));
        entity.setCurrency("CNY");//设置支付币种
        entity.setTradeState("0");//设置支付状态，0：订单生成
        int count = payOrderMapper.insert(entity);
        return PayOrderConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state)
            throws BusinessException {
        LambdaUpdateWrapper<PayOrder> payOrderLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        payOrderLambdaUpdateWrapper.eq(PayOrder::getTradeNo, tradeNo)
                .set(PayOrder::getTradeState, state)
                .set(PayOrder::getPayChannelTradeNo, payChannelTradeNo);
        if (state != null && state.equals("2")){
            payOrderLambdaUpdateWrapper.set(PayOrder::getPaySuccessTime,LocalDateTime.now());
        }
        payOrderMapper.update(null, payOrderLambdaUpdateWrapper);
    }

    @Override
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException {
        //闪聚平台的应用ID
        String appId = payOrderDTO.getAppId();
        //获取微信支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId,
                "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);

        //state是一个原样返回的参数
        String jsonString = JSON.toJSONString(payOrderDTO);
        String state = EncryptUtil.encodeUTF8StringBase64(jsonString);
        try {
            //https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
            String url = String.format("%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect",
                    wxOAuth2RequestUrl,wxConfigParam.getAppId(), wxOAuth2CodeReturnUrl, state
            );

            return "redirect:"+url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "forward:/pay-page-error";
    }

    @Override
    public String getWXOAuthOpenId(String code, String appId) throws BusinessException {

        //获取微信支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId,
                "shanju_c2b", "WX_JSAPI");
        String param = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);

        //https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                oauth2Token, wxConfigParam.getAppId(), wxConfigParam.getAppSecret(), code
        );

        //申请openid，请求url
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //申请openid接口响应的内容，其中包括了openid
        String body = exchange.getBody();
        log.info("申请openid响应的内容:{}",body);
        //获取openid
        String openid = JSON.parseObject(body).getString("openid");
        return openid;
    }

    @Override
    public Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException {
        String openId = payOrderDTO.getOpenId();
        //保存订单
        payOrderDTO.setChannel("WX_JSAPI");//支付渠道
        //保存订单到闪聚平台数据库
        PayOrderDTO save = save(payOrderDTO);
        //调用支付渠道代理服务，调用微信下单接口
        return weChatJsapi(openId,save.getTradeNo());
    }

    /**
     * 微信jsapi 调用支付渠道代理
     * @param openId
     * @param tradeNo
     * @return
     */
    private Map<String, String> weChatJsapi(String openId,String tradeNo){
        //根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        if(payOrderDTO == null){
            throw new BusinessException(CommonErrorCode.E_400002);
        }
        //构造微信订单参数实体
        WeChatBean weChatBean = new WeChatBean();
        weChatBean.setOpenId(openId);//openid
        weChatBean.setSpbillCreateIp(payOrderDTO.getClientIp());//客户ip
        weChatBean.setTotalFee(payOrderDTO.getTotalAmount());//金额
        weChatBean.setBody(payOrderDTO.getBody());//订单描述
        weChatBean.setOutTradeNo(payOrderDTO.getTradeNo());//使用聚合平台的订单号 tradeNo
        weChatBean.setNotifyUrl("none");//异步接收微信通知支付结果的地址(暂时不用)
        //根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO =
                payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(),
                        "shanju_c2b", "WX_JSAPI");
        if(payChannelParamDTO == null){
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        //支付宝渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTO.getParam(),
                WXConfigParam.class);
        return payChannelAgentService.createPayOrderByWeChatJSAPI(wxConfigParam, weChatBean);
    }
}
