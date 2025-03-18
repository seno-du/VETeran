package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.PetDTO;

@Mapper
public interface PetDAO {

    // 특정 펫 조회 (petNum 기준, join 적용)
    Map<String, Object> selectPetByPetNum(@Param("petNum") Long petNum);

    List<Map<String, Object>> selectAllPets(@Param("size") Long size, @Param("offset") Long offset);

    // HospitalAdd 전용 펫 전체 조회 (chartNum 포함)
    List<Map<String, Object>> selectAllPetsForHospital(Long chartNum);

    //------------------------------------------------------------
    // 펫 등록 (김채린)
    void insertMyPet(PetDTO petDTO);

    // 펫 정보 수정 (김채린)
    void updateMyPet(PetDTO petDTO);

    // 펫 비활성화 (김채린) 
    void editPetStatus(Long petNum);

    //userNum으로 펫 조회 (김채린)
    List<Map<String, Object>> findPetByUserNum(@Param("userNum") Long userNum, @Param("size") long size, @Param("start") long start);

    //userNum으로 펫 카운트 (김채린)
    Long countPetByUserNum(Long userNum);

    Long countPet();
}
