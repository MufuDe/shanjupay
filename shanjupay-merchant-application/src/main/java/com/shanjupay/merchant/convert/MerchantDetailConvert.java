package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 商户资质申请的vo和dto进行转换
 */
@Mapper
public interface MerchantDetailConvert {
    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);

    //将DTO转换成VO
    MerchantDetailVO dto2vo(MerchantDTO merchantDTO);

    //将VO转换成DTO
    MerchantDTO vo2dto(MerchantDetailVO merchantDetailVO);
}
