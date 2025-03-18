package com.jjangtrio.veteran.ServerApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.PetDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PetDTO;

@Service
public class PetService {

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PET_KEY = "pet:"; // Redis 키 prefix

    // 특정 펫 조회
    public Map<String, Object> selectPetByPetNum(Long petNum) {
        return petDAO.selectPetByPetNum(petNum);
    }

   // 펫 전체 조회 (보호자 이름 포함)
    public List<Map<String, Object>> selectAllPets(Long size, Long page) {

        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        Long offset = (page - 1) * size;
        return petDAO.selectAllPets(size, offset);
    }

    // HospitalAdd 전용 펫 전체 조회 (chartNum 포함)
    public List<Map<String, Object>> selectAllPetsForHospital(Long chartNum) {
        return petDAO.selectAllPetsForHospital(chartNum);
    }

    //-------------------------------------------------------------------
    // 펫 등록 (김채린)
    public boolean insertMyPet(PetDTO petDTO) {
        try{
            petDAO.insertMyPet(petDTO);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //정윤호
    public Long countPet() {
        return petDAO.countPet();
    }

    // 펫 정보 수정 (김채린)
    public boolean updateMyPet(PetDTO pet) {
        try{
            petDAO.updateMyPet(pet);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 펫 비활성화 (김채린)
    public void editPetStatus(Long petNum) {
        petDAO.editPetStatus(petNum);
    }

    // 펫 userNum으로 조회 (김채린)
    public List<Map<String, Object>> findPetByUserNum(Long userNum, Long size, Long start) {
        return petDAO.findPetByUserNum(userNum, size, start);
    }

    //userNum으로 펫 카운트 (김채린)
    public Long countPetByUserNum(Long userNum) {
        return petDAO.countPetByUserNum(userNum);
    }
  
}
