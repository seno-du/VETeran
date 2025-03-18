package com.jjangtrio.veteran.ServerApplication.dto;

import java.sql.Timestamp;

import org.apache.ibatis.type.Alias;    
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("mfiledto")
public class MfileDTO {
    private Long mfileNum;
    private String mfileTitle;
    private String mfileContent;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp mfileDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp mfileModifiedDate;
    private Long mfileDownloadCount;
    private String mfileUploader;
    private String mfileName;
    private String mfileRealName;
    private Long mfileCategory;
    private String mfileStatus;
}