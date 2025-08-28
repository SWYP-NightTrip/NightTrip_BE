package com.nighttrip.core.global.image.service;

import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.dto.PresignedUploadDto;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

public interface ImageService {
    PresignedUploadDto generatePresignedUrl(String userId, String placeName, int index, String extension);

    List<PresignedUploadDto> generatePresignedUrls(String userId, String placeName, int count, String extension);

    void saveImageData(ImageType imageType, Long relatedId, String filePath, ImageSizeType imageSizeType);

    @Transactional
    void saveImageDataList(ImageType imageType,
                           Long relatedId,
                           List<String> filePaths,
                           ImageSizeType imageSizeType);

    @Transactional
    void saveByCount(ImageType imageType,
                     Long relatedId,
                     String userId,
                     String placeName,
                     int count,
                     String extension,
                     ImageSizeType imageSizeType);

    void deleteObject(String fileName, String fileLocation);

    void deleteFolder(String fileLocation);
}
