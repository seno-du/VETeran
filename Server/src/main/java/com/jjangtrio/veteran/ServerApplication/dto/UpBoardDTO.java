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
@Alias("upboarddto")
public class UpBoardDTO {
    private Long upboardNum;
    private Long userNum;
    private String upboardTitle;
    private String upboardContent;
    private String upboardImgn;
    private Long upboardHit;
    private String upboardReip;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp upboardBdate;
    private Long commentCount;
}

