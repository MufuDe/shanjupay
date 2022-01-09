package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

public interface MerchantService{
    /**
     * 根据ID查询详细信息
     * @param merchantId
     * @return
     */
    MerchantDTO queryMerchantById(Long merchantId);

    /**
     * 根据租户ID查询商户的信息
     * @param tenantId 租户ID
     * @return 商户信息
     */
    public MerchantDTO queryMerchantByTenantId(Long tenantId);
    /**
     * 注册商户服务接口，接收账号、密码、手机号，为了可扩展性使用merchantDTO接收数据
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 资质申请接口
     * @param merchantId 商户ID
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 商户新增门店
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;

    /**
     * 商户新增员工
     * @param staffDTO 员工信息
     * @return 新增成功的员工信息
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 为门店设置管理员
     * @param storeId 门店ID
     * @param staffId 员工ID
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

    /**
     * 分页条件查询商户下门店
     * @param storeDTO 查询条件，门店信息：商户ID
     * @param pageNo 分页页码
     * @param pageSize 分页容量(记录数)
     * @return 分页信息
     */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) throws BusinessException;

    /**
     * 查询门店是否属于某个商户
     * @param storeId 门店ID
     * @param merchantId 商户ID
     * @return true or false
     * @throws BusinessException
     */
    Boolean queryStoreInMerchant(Long storeId, Long merchantId) throws  BusinessException;
}