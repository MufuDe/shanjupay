package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 商户注册的vo和dto进行转换
 */
@org.mapstruct.Mapper
public interface MerchantRegisterConvert {
    MerchantRegisterConvert INSTANCE = Mappers.getMapper(MerchantRegisterConvert.class);

    //将DTO转换成VO
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);

    //将VO转换成DTO
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);
}
