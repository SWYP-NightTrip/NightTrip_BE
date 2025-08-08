package com.nighttrip.core.global.image.service;

import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final String bucket;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    public ImageService(
            @Value("${amazon.aws.accessKey}") String accessKey,
            @Value("${amazon.aws.secretKey}") String secretKey,
            @Value("${amazon.aws.region}") String region,
            @Value("${amazon.aws.bucket}") String bucket,
            @Value("${ncp.object-storage.end-point}") String endpoint,
            ImageRepository imageRepository
    ) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.bucket = bucket;

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.imageRepository = imageRepository;
    }

    /**
     * ✅ Presigned URL 생성
     */
    public Map<String, Object> generatePresignedUrl(String userId, String placeName, int index, String extension) {
        String safePlace = placeName.replaceAll("[^a-zA-Z0-9가-힣_-]", "_");
        String fileName = safePlace + "_" + index + "." + extension;
        String filePath = "user-spot/" + userId + "/" + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filePath)
                .contentType("image/" + extension)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        Map<String, Object> result = new HashMap<>();
        result.put("presignedUrl", presignedUrl);
        result.put("method", "PUT");
        result.put("headers", Map.of("Content-Type", "image/" + extension));
        result.put("filePath", filePath);
        return result;
    }

    public void saveImageData(ImageType imageType, Long relatedId, String filePath, ImageSizeType imageSizeType) {
        String fullUrl = "https://" + bucket + ".kr.object.ncloudstorage.com/" + filePath;
        ImageUrl imageUrl = new ImageUrl(imageType, relatedId, fullUrl, imageSizeType);
        imageRepository.save(imageUrl);
    }

    @Transactional
    public void saveImageDataList(ImageType imageType,
                                  Long relatedId,
                                  List<String> filePaths,
                                  ImageSizeType imageSizeType) {

        // URL 생성 및 엔티티 리스트 변환
        List<ImageUrl> imageUrls = filePaths.stream()
                .map(filePath -> {
                    String fullUrl = "https://" + bucket + ".kr.object.ncloudstorage.com/" + filePath;
                    return new ImageUrl(imageType, relatedId, fullUrl, imageSizeType);
                })
                .toList();

        // 대량 저장
        imageRepository.saveAll(imageUrls);
    }



    /**
     * ✅ 단일 객체 삭제
     */
    public void deleteObject(String fileName, String fileLocation) {
        String key = fileLocation + "/" + fileName;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (SdkException e) {
            throw new RuntimeException("객체 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * ✅ 폴더 삭제 (prefix 기준)
     */
    public void deleteFolder(String fileLocation) {
        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(fileLocation + "/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            List<ObjectIdentifier> toDelete = listResponse.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(toDelete).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

        } catch (SdkException e) {
            throw new RuntimeException("폴더 삭제 실패: " + e.getMessage());
        }
    }
}