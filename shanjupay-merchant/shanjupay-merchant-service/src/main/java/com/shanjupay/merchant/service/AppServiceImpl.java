package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@org.apache.dubbo.config.annotation.Service
public class AppServiceImpl implements AppService {

    @Autowired
    AppMapper appMapper;
    @Autowired
    MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {

        String appName = appDTO.getAppName();
        if (merchantId == null || appDTO == null || StringUtils.isBlank(appName)){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验商户是否通过资质审核
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //取出商户资质申请的状态
        String auditStatus = merchant.getAuditStatus();
        if (!auditStatus.equals("2")){
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        //校验应用名称的唯一性
        //传入的应用名称
        if (isExistAppName(appName)){
            throw new BusinessException(CommonErrorCode.E_200004);
        };
        //生成应用ID
        String appId = UUID.randomUUID().toString();
        //保存商户应用信息
        //调用appMapper向app表插入数据
        App entity = AppCovert.INSTANCE.dto2entity(appDTO);
        entity.setAppId(appId);
        entity.setMerchantId(merchantId);

        appMapper.insert(entity);
        return AppCovert.INSTANCE.entity2dto(entity);
    }

    //判断应用名称是否存在
    private Boolean isExistAppName(String appName){
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>().eq(App::getAppName, appName));
        if (count > 0){
            return true;
        }
        return false;
    }

    /**
     * 获取对应商户的应用信息列表
     * @param merchantId 商户ID
     * @return 应用信息列表
     * @throws BusinessException
     */
    @Override
    public List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException {
        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listentity2dto(apps);
        return appDTOS;
    }

    /**
     * 获取某个具体的应用信息
     * @param appId 应用ID
     * @return 具体的应用信息
     * @throws BusinessException
     */
    @Override
    public AppDTO getAppById(String appId) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, appId));
        AppDTO appDTO = AppCovert.INSTANCE.entity2dto(app);
        return appDTO;
    }

    /**
     * 判断某个应用是否属于某个商户
     * @param appId 应用ID
     * @param merchantId 商户ID
     * @return true or false
     * @throws BusinessException
     */
    @Override
    public Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException {
        Integer count = appMapper.selectCount(new LambdaQueryWrapper<App>()
                .eq(App::getAppId, appId)
                .eq(App::getMerchantId, merchantId));
        return count > 0;
    }
}
