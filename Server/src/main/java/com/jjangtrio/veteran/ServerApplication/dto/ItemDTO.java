package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("itemdto")
public class ItemDTO {
    private String itemId; // 제품 고유 ID
    private String itemCategory; // 제품 카테고리
    private String itemName; // 제품 이름
    private Long itemPrice; // 제품 가격
    private String itemState; // 제품 상태 ('일반' 또는 '마약')
}
