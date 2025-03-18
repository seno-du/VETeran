package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("medicalfiledto")
public class MedicalfileDTO {
    private Long medicalNum;
    private Long managerNum;
    private Long chartNum;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date medicalDate;

    private String medicalNote;
    private String studyUid;
    private String seriesUid;
    private String instanceUid;
}