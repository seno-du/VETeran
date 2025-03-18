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
@Alias("petdto")
public class PetDTO {

    private Long petNum;
    private Long userNum;
    private String petSpecies;
    private String petColor;
    private String petName;
    private String petBreed;
    private String petGender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date petBirth;
    private String petMicrochip;
    private Double petWeight;
    private String petStatus;
    private String petImage;
}
