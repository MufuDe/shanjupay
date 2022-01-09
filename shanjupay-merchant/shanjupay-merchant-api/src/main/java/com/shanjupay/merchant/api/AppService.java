package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * 应用管理相关的接口
 */
public interface AppService {
    //应用的信息，商户id

    /**
     * 创建应用
     * @param merchantId 商户ID
     * @param appDTO 应用信息
     * @return 创建成功的应用信息
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException;

    /**
     * 查询商户下的应用列表
     * @param merchantId 商户ID
     * @return 应用信息的集合
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;

    /**
     * 查询应用
     * @param appId 应用ID
     * @return 应用信息
     * @throws BusinessException
     */
    AppDTO getAppById(String appId) throws BusinessException;

    /**
     * 查询应用是否属于某个商户
     * @param appId 应用ID
     * @param merchantId 商户ID
     * @return true or false
     * @throws BusinessException
     */
    Boolean queryAppInMerchant(String appId, Long merchantId) throws BusinessException;
}
