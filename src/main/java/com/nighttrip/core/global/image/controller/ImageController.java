package com.nighttrip.core.global.image.controller;

import com.nighttrip.core.global.dto.ApiResponse;
import com.nighttrip.core.global.image.dto.PresignedUploadDto;
import com.nighttrip.core.global.image.dto.PresignedUrlBatchRequest;
import com.nighttrip.core.global.image.dto.PresignedUrlRequest;
import com.nighttrip.core.global.image.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /** Presigned 단건 */
    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<PresignedUploadDto>> getPresignedUrl(@RequestBody PresignedUrlRequest request) {
        PresignedUploadDto dto = imageService.generatePresignedUrl(
                request.userId(),
                request.placeName(),
                request.index(),
                request.extension()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(dto));
    }

    @PostMapping("/presign/batch")
    public ResponseEntity<ApiResponse<List<PresignedUploadDto>>> getPresignedUrls(@RequestBody PresignedUrlBatchRequest request) {
        List<PresignedUploadDto> list = imageService.generatePresignedUrls(
                request.userId(),
                request.placeName(),
                request.count(),
                request.extension()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(list));
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

