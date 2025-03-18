package com.jjangtrio.veteran.ServerApplication.API.Compreface;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ComprefaceService {

    @Value("${COMPREFACE-API-KEY}")
    private String apiKey;

    private final String urlString = "http://192.168.0.92:8000/api/v1/verification/verify";

    public Boolean faceRecognition(MultipartFile sourceImage, MultipartFile targetImage) {

        System.out.println("Source Image: " + sourceImage.getOriginalFilename());
        System.out.println("Target Image: " + targetImage.getOriginalFilename());

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("x-api-key", apiKey);

            // MultipartFile을 ByteArrayResource로 변환
            ByteArrayResource sourceImageResource = new ByteArrayResource(sourceImage.getBytes()) {
                @Override
                public String getFilename() {
                    return sourceImage.getOriginalFilename();
                }
            };

            ByteArrayResource targetImageResource = new ByteArrayResource(targetImage.getBytes()) {
                @Override
                public String getFilename() {
                    return targetImage.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("source_image", sourceImageResource); // 첫 번째 이미지
            body.add("target_image", targetImageResource); // 두 번째 이미지

            // HttpEntity 생성 (headers + body)
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // API 호출 (POST 요청)
            ResponseEntity<String> response = restTemplate.exchange(urlString, HttpMethod.POST, requestEntity, String.class);

            String jsonResponse = response.getBody();
            // JSON 응답 처리
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonResponse);

                JsonNode similarityNode = rootNode
                        .path("result")   
                        .get(0)           
                        .path("face_matches")  
                        .get(0)           
                        .path("similarity");   

                // 유사도 값 출력 및 반환
                if (!similarityNode.isMissingNode()) {
                    double similarity = similarityNode.asDouble();
                    System.out.println("유사도: " + similarity);
                    return similarity > 0.98;
                } else {
                    System.out.println("유사도 정보를 찾을 수 없습니다.");
                    return false;
                }

            } catch (Exception e) {
                System.out.println("응답 파싱 오류: " + e.getMessage());
                return false;
            }

        } catch (IOException e) {
            System.out.println("파일 처리 오류: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("얼굴 인식 오류: " + e.getMessage());
            return false;
        }
    }
}
