package com.nighttrip.core.global.image.controller;

import com.nighttrip.core.global.image.dto.PresignedUrlRequest;
import com.nighttrip.core.global.image.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/presign")
    public ResponseEntity<Map<String, Object>> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        Map<String, Object> url = imageService.generatePresignedUrl(
                request.userId(),
                request.placeName(),
                request.index(),
                request.extension()
        );
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/delete-object")
    public void deleteObject(@RequestParam String fileName,
                             @RequestParam String fileLocation) {
        imageService.deleteObject(fileName, fileLocation);
    }

    @DeleteMapping("/delete-folder")
    public void deleteFolder(@RequestParam String fileLocation) {
        imageService.deleteFolder(fileLocation);
    }
}

