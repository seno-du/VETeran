package com.jjangtrio.veteran.ServerApplication.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjangtrio.veteran.ServerApplication.dao.PetDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PetDTO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.PetService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/pet")
@CrossOrigin(origins = "http://localhost:5000", allowCredentials = "true")
public class PetController {

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private PetService petService;

    @Autowired
    private UserService userService;

    @Value("${image.log.path}")
    private String uploadDir;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PET_LIST_KEY = "allPets";

    @PostConstruct
    public void init() {
        if (uploadDir.isEmpty()) {
            uploadDir = new File(System.getProperty("user.dir"), "Server/uploads").getAbsolutePath();
        }

        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            boolean isCreated = uploadPath.mkdirs();
            System.out.println("✅ 업로드 디렉토리 생성됨: " + uploadPath.getAbsolutePath());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> selectAllPets(@RequestParam(value = "page", defaultValue = "1") Long page) {
        try {
            Long size = 15L;

            List<Map<String, Object>> allPets = petService.selectAllPets(size, page);
            Long count = petService.countPet();

            Map<String, Object> result = new HashMap<>();

            result.put("data", allPets);
            result.put("total", count);
            result.put("currentPage", page);
            result.put("size", size);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 펫 조회
     */
    @GetMapping("/{petNum}")
    public ResponseEntity<?> getPet(@PathVariable("petNum") Long petNum) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null)
                return ResponseEntity.status(406).body("사용자를 찾을 수 없음");

            Map<String, Object> result = petService.selectPetByPetNum(petNum);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error retrieving pet info: " + e.getMessage());
        }
    }

    // HospitalAdd 전용 펫 전체 조회 (chartNum 포함)
    @GetMapping("/allForHospital")
    public ResponseEntity<?> getAllPetsForHospital() {
        try {
            List<Map<String, Object>> result = petService.selectAllPetsForHospital(null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error retrieving all pet info for hospital: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> insertMyPet(@RequestPart("pet") String map,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null)
                return ResponseEntity.status(406).body("사용자를 찾을 수 없음");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> petData = objectMapper.readValue(map, new TypeReference<>() {
            });

            PetDTO petDTO = new PetDTO();
            petDTO.setUserNum(userNum);
            petDTO.setPetSpecies((String) petData.get("petSpecies"));
            petDTO.setPetColor((String) petData.get("petColor"));
            petDTO.setPetName((String) petData.get("petName"));
            petDTO.setPetBreed((String) petData.get("petBreed"));

            if (petData.get("petIsNeutered").equals("예")) {
                petDTO.setPetGender("중성화" + (String) petData.get("petGender"));
            } else {
                petDTO.setPetGender((String) petData.get("petGender"));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            petDTO.setPetBirth(sdf.parse(petData.get("petBirth").toString()));

            petDTO.setPetWeight(Double.valueOf(petData.get("petWeight").toString()));

            if (petData.get("petMicrochip") != null && petData.get("petMicrochip") instanceof String) {
                petDTO.setPetMicrochip((String) petData.get("petMicrochip"));
            }

            if (file != null && !file.isEmpty()) {
                System.out.println("file name : " + file.getOriginalFilename());

                try {
                    String originalFileName = file.getOriginalFilename();
                    String newFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        
                    // 업로드할 디렉토리 경로 설정
                    String uploadDir = System.getProperty("user.dir") + File.separator + "Veteran_Back/Server/src/main/resources/static/uploads";
                    System.out.println("경로========================" + uploadDir);
                    File uploadPath = new File(uploadDir);
        
                    // 디렉토리가 없으면 생성
                    if (!uploadPath.exists()) {
                        uploadPath.mkdirs(); // 디렉토리가 없다면 생성
                    }
        
                    // 파일 경로 생성
                    Path path = Paths.get(uploadDir, newFileName);
                    Files.write(path, file.getBytes()); // 파일 저장
        
                petDTO.setPetImage(newFileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (petService.insertMyPet(petDTO))
                return ResponseEntity.status(200).body("펫 등록 성공");
            return ResponseEntity.status(406).body("펫 등록 실패");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("Error inserting pet: " + e.getMessage());
        }
    }

    // userNum으로 pet 조회
    // http://localhost:7124/back/api/pet/userNum/{userNum}?page={page}://localhost:5000")
    @GetMapping("/userNum")
    public ResponseEntity<?> findHospitalLogByUserNum(@RequestParam(name = "page") long page) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null)
                return ResponseEntity.status(406).body("존재하지 않는 회원입니다.");

            long count = petService.countPetByUserNum(userNum);

            Map<String, Object> result = new HashMap<>();

            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(4L)
                    .totalRecords(count)
                    .totalPages((long) (Math.ceil((double) count / 4)))
                    .startIndex((long) ((page - 1) * 4))
                    .endIndex((long) Math.min(page * 4, count))
                    .hasPrevPage(page > 1)
                    .hasNextPage(page < (int) Math.ceil((double) count / 4))
                    .build();

            result.put("PageDTO", pdto);
            result.put("list", petService.findPetByUserNum(userNum, pdto.getPageSize(), pdto.getStartIndex()));

            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // 팻 정보 수정
    // http://localhost:7124/back/api/pet/update/{petNum}
    // Headers -> Content-Type: multipart/form-data
    // Body -> form-data
    // file: MultipartFile
    // pet :
    // {
    // "petGender" : "암컷",
    // "petIsNeutered" : "예",
    // "petWeight" : 11.01,
    // "petBirth" : "2000-01-01",
    // "petMicrochip" : "00000004"
    // }
    @PostMapping("/update/{petNum}")
    public ResponseEntity<?> updateMyPet(@RequestPart("pet") String map,
            @RequestPart("file") MultipartFile file, @PathVariable("petNum") Long petNum) {

        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null)
                return ResponseEntity.status(406).body("사용자를 찾을 수 없음");

            Map<String, Object> pet = petService.selectPetByPetNum(petNum);
            if (pet == null)
                return ResponseEntity.status(406).body("펫을 찾을 수 없음");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> petData = objectMapper.readValue(map, new TypeReference<>() {
            });

            PetDTO petDTO = new PetDTO();
            petDTO.setPetNum(petNum);

            if (petData.get("petIsNeutered") == null) {
                petDTO.setPetGender((String) petData.get("petGender"));
            } else {
                if (petData.get("petIsNeutered").equals("예")) {
                    petDTO.setPetGender("중성화" + (String) petData.get("petGender"));
                } else {
                    petDTO.setPetGender((String) petData.get("petGender"));
                }
            }

            petDTO.setPetWeight(Double.valueOf(petData.get("petWeight").toString()));

            
            if (file != null && !file.isEmpty()) {
                System.out.println("file name : " + file.getOriginalFilename());

                try {
                    String originalFileName = file.getOriginalFilename();
                    String newFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        
                    // 업로드할 디렉토리 경로 설정
                    String uploadDir = System.getProperty("user.dir") + File.separator + "Veteran_Back/Server/src/main/resources/static/uploads";
                    System.out.println("경로========================" + uploadDir);
                    File uploadPath = new File(uploadDir);
        
                    // 디렉토리가 없으면 생성
                    if (!uploadPath.exists()) {
                        uploadPath.mkdirs(); // 디렉토리가 없다면 생성
                    }
        
                    // 파일 경로 생성
                    Path path = Paths.get(uploadDir, newFileName);
                    Files.write(path, file.getBytes()); // 파일 저장
        
                petDTO.setPetImage(newFileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (petService.updateMyPet(petDTO))
                return ResponseEntity.status(200).body("펫 수정 성공");

            return ResponseEntity.status(500).body("펫 수정 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating pet: " + e.getMessage());
        }
    }

    // 팻 무지개다리
    // http://localhost:7124/back/api/pet/unsubscription/104
    @PostMapping("/unsubscription/{petNum}")
    public ResponseEntity<?> editPetStatus(@PathVariable("petNum") Long petNum,
            @RequestBody Map<String, Object> password) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            Map<String, Object> pet = petService.selectPetByPetNum((long) petNum);
            UserDTO user = userService.selectUserNum(userNum);

            if (pet != null && pet.get("petStatus").equals("활성")) {
                password.put("userPwd", user.getUserPwd());
                if (userService.verifyPwd(password)) {
                    petService.editPetStatus(petNum);
                    return ResponseEntity.ok().body("펫 정보 삭제가 완료되었습니다.");
                }
            }
            return ResponseEntity.status(406).body("이미 처리된 서비스 입니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("서비스 실행에 실패했습니다.");
        }
    }

}
