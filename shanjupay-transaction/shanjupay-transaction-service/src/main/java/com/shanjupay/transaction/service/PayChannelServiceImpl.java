package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    private PlatformChannelMapper platformChannelMapper;
    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;
    @Autowired
    private PayChannelParamMapper payChannelParamMapper;
    @Resource
    private Cache cache;
    /**
     * 查询平台服务
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        //查询platform_channel表的全部记录
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        //将platformChannels的entity实体类转换成platformChannelDTOS的list DTO
        List<PlatformChannelDTO> platformChannelDTOS = PlatformChannelConvert.INSTANCE.
                listentity2listdto(platformChannels);
        return platformChannelDTOS;
    }

    /**
     * 为应用APP和服务类型PlatformChannel进行绑定
     * @param appId 应用ID
     * @param platformChannelCodes 平台服务类型列表
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        //根据appId和平台服务类型code查询app_platform_channel
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new
                LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        //如果没有绑定则绑定
        if (appPlatformChannel == null){
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId); //应用ID
            appPlatformChannel.setPlatformChannel(platformChannelCodes); //服务类型Code
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    /**
     * 应用是否已经绑定了某个服务类型
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     * @throws BusinessException
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        Integer count = appPlatformChannelMapper.selectCount(new
                LambdaQueryWrapper<AppPlatformChannel>()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (count > 0){
            //已存在绑定关系返回1
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        //调用mapper查询数据库pay_channel、platform_channel、platform_pay_channel
        return platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    /**
     * 保存支付渠道参数
     * @param payChannelParam 商户原始支付渠道参数：商户id，包括应用id，服务类型code，支付渠道code，配置名称，配置参数（json数据格式）
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException {
        if (payChannelParam == null || payChannelParam.getChannelName() == null || payChannelParam.getParam() == null
        || payChannelParam.getAppId() == null || payChannelParam.getPlatformChannelCode() == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据应用、服务类型、支付渠道查询一条记录
        //根据应用、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParam.getAppId(),
                payChannelParam.getPlatformChannelCode());
        if (appPlatformChannelId == null){
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用与服务类型的绑定id和支付渠道查询PayChannelParam的一条记录
        PayChannelParam entity = payChannelParamMapper.selectOne(
                new LambdaQueryWrapper<PayChannelParam>()
                        .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                        .eq(PayChannelParam::getPayChannel,payChannelParam.getPayChannel()));
        //如果存在配置则更新配置
        if (entity != null){
            entity.setChannelName(payChannelParam.getChannelName()); //配置名称
            entity.setParam(payChannelParam.getParam()); //json格式的参数
            payChannelParamMapper.updateById(entity);
        }else {
            //否则添加配置
            PayChannelParam entityNew = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParam);
            entityNew.setId(null);
            entityNew.setAppPlatformChannelId(appPlatformChannelId); //应用与服务类型的绑定id
            payChannelParamMapper.insert(entityNew);
        }

        //保存到redis
        updateCache(payChannelParam.getAppId(),payChannelParam.getPlatformChannelCode());
    }

    /**
     * 根据appid和服务类型查询应用与服务类型绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId, String platformChannelCode){
        //根据appid和服务类型查询应用与服务类型绑定id
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(
                new LambdaQueryWrapper<AppPlatformChannel>()
                        .eq(AppPlatformChannel::getAppId, appId)
                        .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel != null) {
            return appPlatformChannel.getId(); //应用与服务类型的绑定id
        }
        return null;
    }

    /**
     * 根据应用和服务类型将查询到的支付渠道参数配置列表写入Redis
     * @param appId 应用ID
     * @param platformChannelCode 服务类型Code
     */
    private void updateCache(String appId, String platformChannelCode){
        //得到redis中key（支付渠道参数列表的key）
        //格式："SJ_PAY_PARAM:appId:platformChannelCode"
        //例如："SJ_PAY_PARAM:855e2883-e4be-4ac9-9cbc-11c2d8ffaee3:shanju_c2b"
        String redisKey = RedisUtil.keyBuilder(appId, platformChannelCode);
        //根据key查询redis
        Boolean exists = cache.exists(redisKey);
        if (exists){
            cache.del(redisKey);
        }
        //根据应用id和服务类型code查询支付渠道参数列表，将支付渠道参数写入redis
        //List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannelCode);
        //根据应用和服务类型找到他们的绑定ID
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannelCode);
        if (appPlatformChannelId != null){
            //应用和服务类型绑定ID查询支付渠道参数记录
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(
                    new LambdaQueryWrapper<PayChannelParam>()
                            .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.
                    listentity2listdto(payChannelParams);
            //将支付渠道参数列表存储到Redis中
            //将payChannelParamDTOS转成JSON数据存入redis
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }
    }

    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return 支付渠道参数列表
     * @throws BusinessException
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel)
            throws BusinessException {
        //先从Redis查询，如果查询到则直接返回，否则从数据库查询。从数据库查询完毕再将数据保存到redis。
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(redisKey);
        if (exists){
            //从redis获取支付渠道参数列表（json字符串）
            String payChannelParamDTOs_String = cache.get(redisKey);
            //将json转为List<PayChannelParamDTO>集合
            List<PayChannelParamDTO> payChannelParamDTOS = JSON.parseArray(payChannelParamDTOs_String,
                    PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }

        //根据应用和服务类型找到他们的绑定ID
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if (appPlatformChannelId == null){
            return null;
        }
        //应用和服务类型绑定ID查询支付渠道参数记录
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(
                new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.
                listentity2listdto(payChannelParams);
        //将payChannelParamDTOS数据存入redis
        updateCache(appId,platformChannel);
        return payChannelParamDTOS;
    }

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @param payChannel 支付渠道
     * @return 支付渠道参数
     * @throws BusinessException
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel,
                                                                   String payChannel) throws BusinessException {
        //根据应用和服务类型查询支付渠道参数列表
        List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        for (PayChannelParamDTO payChannelParamDTO : payChannelParamDTOS) {
            if (payChannelParamDTO.getPayChannel().equals(payChannel)){
                return payChannelParamDTO;
            }
        }
        return null;
    }
}
