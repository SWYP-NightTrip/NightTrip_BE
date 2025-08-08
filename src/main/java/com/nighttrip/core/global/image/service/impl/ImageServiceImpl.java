package com.nighttrip.core.global.image.service.impl;

import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.dto.PresignedUploadDto;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.image.service.ImageService;
import com.nighttrip.core.global.image.utils.UrlUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {

    private final String bucket;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    public ImageServiceImpl(
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
     * ✅ Presigned URL 생성(단건)
     */
    @Override
    public PresignedUploadDto generatePresignedUrl(String userId, String placeName, int index, String extension) {
        String key = UrlUtil.buildRawKey(userId, placeName, index, extension);
        String contentType = "image/" + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/" + extension)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String publicUrl = UrlUtil.buildPublicUrl(bucket, userId, placeName, index, extension);


        return new PresignedUploadDto(
                key,
                presignedUrl,
                "PUT",
                Map.of("Content-Type", contentType),
                publicUrl
        );
    }

    /** ✅ Presigned URL 생성 (일괄) */
    @Override
    public List<PresignedUploadDto> generatePresignedUrls(String userId, String placeName, int count, String extension) {
        if (count <= 0) throw new IllegalArgumentException("count must be > 0");
        List<PresignedUploadDto> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            list.add(generatePresignedUrl(userId, placeName, i, extension));
        }
        return list;
    }

    @Override
    public void saveImageData(ImageType imageType, Long relatedId, String filePath, ImageSizeType imageSizeType) {
        String fullUrl = "https://" + bucket + ".kr.object.ncloudstorage.com/" + filePath;
        ImageUrl imageUrl = new ImageUrl(imageType, relatedId, fullUrl, imageSizeType);
        imageRepository.save(imageUrl);
    }

    @Transactional
    @Override
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

    /** ✅ count만 받아 placeName+index 규칙으로 URL 재구성 후 저장 */
    @Transactional
    @Override
    public void saveByCount(ImageType imageType,
                            Long relatedId,
                            String userId,
                            String placeName,
                            int count,
                            String extension,
                            ImageSizeType imageSizeType) {
        if (count <= 0) return;

        List<ImageUrl> imageUrls = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            String fullUrl = UrlUtil.buildPublicUrl(bucket, userId, placeName, i, extension); // 조회용: 인코딩 적용
            imageUrls.add(new ImageUrl(imageType, relatedId, fullUrl, imageSizeType));
        }
        imageRepository.saveAll(imageUrls);
    }


    /**
     * ✅ 단일 객체 삭제
     */
    @Override
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
    @Override
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