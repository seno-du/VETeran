package com.jjangtrio.veteran.ServerApplication.API.Compreface;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jjangtrio.veteran.ServerApplication.service.ManagerService;

@RestController
@RequestMapping("/api/compreface")
public class ComprefaceController {

    @Autowired
    private ComprefaceService comprefaceService;

    private static final String IMAGE_DIRECTORY = "classpath:/static/imageForCompreface";

    @Autowired
    private ManagerService managerService;

    @PostMapping
    public ResponseEntity<?> faceRecognition(@RequestPart ("source_image") MultipartFile sourceImage,
            @RequestPart ("target_image") MultipartFile targetImage) {

        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        // Map<String, Object> manager = managerService.getManagerById(managerNum);

        // // if (manager == null)
        // // return ResponseEntity.status(401).body("접근 불가능.");

        if (comprefaceService.faceRecognition(sourceImage, targetImage))
            return ResponseEntity.status(200).body("접근 가능");

        return ResponseEntity.status(406).body("접근 불가능");
    }

    
    @GetMapping("/getTargetImages")
    public List<String> getTargetImages() {
        List<String> imagePaths = new ArrayList<>();

        File folder = new File(IMAGE_DIRECTORY);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png")); // 원하는 확장자 필터링
            if (files != null) {
                for (File file : files) {
                    imagePaths.add(file.getName());
                }
            }
        }

        return imagePaths;
    }
}
