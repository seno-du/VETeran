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
@Alias("noticedto")
public class NoticeDTO {

    private Long noticeNum;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp noticeDate;
    private String noticeTitle;
    private String noticeContent;
    private String noticeImage;
    private Long noticeHit;
    private String noticeStatus;
}