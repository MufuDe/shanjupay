package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.apache.dubbo.config.annotation.Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;
    @Autowired
    StoreMapper storeMapper;
    @Autowired
    StaffMapper staffMapper;
    @Autowired
    StoreStaffMapper storeStaffMapper;
    @org.apache.dubbo.config.annotation.Reference
    TenantService tenantService;
    /**
     * 根据ID查询商铺详情
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        //        MerchantDTO merchantDTO = new MerchantDTO();
        //        merchantDTO.setId(merchant.getId());
        //        merchantDTO.setMerchantName(merchant.getMerchantName());
        //设置其它属性...
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 根据租户ID查询商户的信息
     * @param tenantId 租户ID
     * @return 商户信息
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getTenantId, tenantId));
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 商户服务商户注册的定义
     * 调用SaaS接口：新增租户、用户、绑定租户和用户的关系，初始化权限
     * @param merchantDTO 商户注册信息
     * @return 注册成功的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        //校验参数的合法性
        if (merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        //校验手机号是否为空
        if (StringUtils.isBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //校验用户密码是否为空
        if (StringUtils.isBlank(merchantDTO.getPassword())){
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //手机号格式校验
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //校验手机号唯一性
        //根据手机号查询商户表，如果存在记录则说明手机号已存在
        Integer count = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().
                eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count > 0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //更新，调用SaaS接口
        //构造调用参数
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        //接口参数：手机号、账号、密码、租户类型、默认套餐、租户名称(同账号名)
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");//租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");//套餐，根据套餐进行权限分配
        createTenantRequestDTO.setName(merchantDTO.getUsername());
        //如果租户已经在SaaS中存在，SaaS直接返回此租户的信息，否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);
        //获取租户的id
        Long tenantId = tenantAndAccount.getId();
        if (tenantAndAccount == null || tenantId == null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }
        //租户的id在商户表唯一
        //根据租户的id从商户表查询，如果存在记录则不允许添加商户
        Integer tenantCount = merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getTenantId, tenantId));
        if (tenantCount > 0){
            throw new BusinessException(CommonErrorCode.E_200017);
        }
        //Merchant merchant = new Merchant();
        //merchant.setMobile(merchantDTO.getMobile());
        //写入其他属性
        //使用MapStruct进行对象转换
        Merchant merchant = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
        //设置所对应的租户ID
        merchant.setTenantId(tenantId);
        //设置审核的状态为0-未进行资质申请
        merchant.setAuditStatus("0");
        //调用mapper向数据库写入记录
        merchantMapper.insert(merchant);
        //新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId()); //商户ID
        //storeDTO.setStoreStatus(true);//门店状态
        StoreDTO store = createStore(storeDTO);
        //新增员工
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile()); //手机号
        staffDTO.setUsername(merchantDTO.getUsername()); //账号
        staffDTO.setStoreId(store.getId()); //员工所属门店ID
        staffDTO.setMerchantId(merchant.getId()); //商户ID
        //staffDTO.setStaffStatus(true);//员工状态为启用
        StaffDTO staff = createStaff(staffDTO);
        //新增管理员
        bindStaffToStore(store.getId(), staff.getId());
        //将DTO写入新增商户的ID
        //merchantDTO.setId(merchant.getId());
        //将Entity转换成DTO
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 资质申请
     * @param merchantId 商户ID
     * @param merchantDTO 资质申请的信息
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantDTO == null || merchantId == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //校验商户ID的合法性
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        //将DTO转成Entity
        Merchant entity = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
        //将必要的参数设置到Entity当中
        entity.setId(merchant.getId());
        entity.setMobile(merchant.getMobile());//因为资质申请的时候手机号不能改，所以得使用数据库内的手机号
        entity.setAuditStatus("1");
        entity.setTenantId(merchant.getTenantId());
        //调用mapper更新商户表
        merchantMapper.updateById(entity);

    }

    /**
     * 商户新增门店
     * @param storeDTO 门店信息
     * @return 新增成功的门店信息
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("商户下新增门店:{}" + JSON.toJSONString(store));
        //新增门店
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    /**
     * 商户新增员工
     * @param staffDTO 员工信息
     * @return 新增成功的员工信息
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //参数合法性校验
        if (staffDTO == null || StringUtils.isBlank(staffDTO.getMobile())
                || StringUtils.isBlank(staffDTO.getUsername())
                || staffDTO.getStoreId() == null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //在同一个商户下员工的账号和手机号唯一
        //账号username唯一性校验
        boolean existStaffByUserName = isExistStaffByUserName(staffDTO.getUsername(), staffDTO.getMerchantId());
        if (existStaffByUserName){
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        //手机号唯一性校验
        boolean existStaffByMobile = isExistStaffByMobile(staffDTO.getMobile(), staffDTO.getMerchantId());
        if (existStaffByMobile){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        Staff entity = StaffConvert.INSTANCE.dto2entity(staffDTO);
        //设置员工所属门店
        log.info("商户下新增员工:{}" + JSON.toJSONString(entity));
        staffMapper.insert(entity);
        return StaffConvert.INSTANCE.entity2dto(entity);
    }

    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByMobile(String mobile, Long merchantId){
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getMobile, mobile)
                .eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param username
     * @param merchantID
     * @return
     */
    private boolean isExistStaffByUserName(String username, Long merchantID){
        Integer count = staffMapper.selectCount(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, username)
                .eq(Staff::getMerchantId, merchantID));
        return count > 0;
    }

    /**
     * 为门店设置管理员
     * @param storeId 门店ID
     * @param staffId 员工ID
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);//管理员ID
        storeStaff.setStoreId(storeId);//门店ID
        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 分页条件查询商户下门店
     * @param storeDTO 查询条件，门店信息：商户ID
     * @param pageNo 分页页码
     * @param pageSize 分页容量(记录数)
     * @return 门店信息分页信息
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {
        //分页条件
        Page<Store> page = new Page<>(pageNo, pageSize);
        //查询条件拼装
        LambdaQueryWrapper<Store> storeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //如果传入商户ID，此时需要拼装查询条件
        if (storeDTO != null && storeDTO.getMerchantId() != null){
            storeLambdaQueryWrapper.eq(Store::getMerchantId, storeDTO.getMerchantId());
        }
        //查询数据库
        storeMapper.selectPage(page,storeLambdaQueryWrapper);
        //将包含entity的list转为包含dto的list
        List<StoreDTO> storeDTOS = StoreConvert.INSTANCE.listentity2dto(page.getRecords());
        return new PageVO<StoreDTO>(storeDTOS,page.getTotal(),pageNo,pageSize);
    }

    /**
     * 查询某个门店是否属于某个商户
     * @param storeId 门店ID
     * @param merchantId 商户ID
     * @return true or false
     * @throws BusinessException
     */
    @Override
    public Boolean queryStoreInMerchant(Long storeId, Long merchantId) throws BusinessException {
        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>()
                .eq(Store::getId, storeId)
                .eq(Store::getMerchantId, merchantId));
        return count > 0;
    }

}