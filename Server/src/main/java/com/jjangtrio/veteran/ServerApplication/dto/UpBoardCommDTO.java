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
@Alias("upboardcommdto")
public class UpBoardCommDTO {
    private Long upboardCommNum;
    private Long upboardNum;
    private Long parentNum;
    private Long userNum;
    private String upboardCommContent;
    private String upboardCommReip;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp upboardCommDate;

}