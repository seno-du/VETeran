package com.jjangtrio.veteran.ServerApplication.dto;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("noticeimagedto")
public class NoticeimageDTO {

    private Long noticeimageNum;
    private Long noticeNum;
    private String noticeimageName;
}
