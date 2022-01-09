package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 定义DTO和entity之间的转换规则
 */
@org.mapstruct.Mapper //对象属性的映射
public interface MerchantCovert{
    //创建转换类实例
    MerchantCovert INSTANCE = Mappers.getMapper(MerchantCovert.class);

    //把DTO转换成Entity
    Merchant dto2entity(MerchantDTO merchantDTO);

    //把Entity转换成DTO
    MerchantDTO entity2dto(Merchant merchant);

    //list之间互相转换
    List<MerchantDTO> entitylist2dtolist(List<Merchant> merchants);
}