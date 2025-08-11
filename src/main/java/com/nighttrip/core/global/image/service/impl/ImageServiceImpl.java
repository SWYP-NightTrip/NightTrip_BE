package com.nighttrip.core.global.image.service.impl;

import com.nighttrip.core.global.enums.ImageSizeType;
import com.nighttrip.core.global.enums.ImageType;
import com.nighttrip.core.global.image.dto.PresignedUploadDto;
import com.nighttrip.core.global.image.entity.ImageUrl;
import com.nighttrip.core.global.image.repository.ImageRepository;
import com.nighttrip.core.global.image.service.ImageService;
import com.nighttrip.core.global.image.utils.UrlUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

import static java.net.URLEncoder.*;
import static java.nio.charset.StandardCharsets.*;

@Service
public class ImageServiceImpl implements ImageService {

    private final String bucket;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    public ImageServiceImpl(
            @Value("${ncp.object-storage.access-key}") String accessKey,
            @Value("${ncp.object-storage.secret-key}") String secretKey,
            @Value("${ncp.object-storage.region}") String region,
            @Value("${ncp.object-storage.bucket}") String bucket,
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
        String key = filePath.replaceAll("^/+", "").replaceAll("/+$", ""); // 양끝 슬래시 정리
        String encodedKey = encodePathSegments(key); // 세그먼트 단위 인코딩
        String fullUrl = "https://kr.object.ncloudstorage.com/" + bucket + "/" + encodedKey; // path-style
        imageRepository.save(new ImageUrl(imageType, relatedId, fullUrl, imageSizeType));
    }

    @Transactional
    @Override
    public void saveImageDataList(ImageType imageType,
                                  Long relatedId,
                                  List<String> filePaths,
                                  ImageSizeType imageSizeType) {

        List<ImageUrl> imageUrls = filePaths.stream()
                .map(fp -> {
                    String key = fp.replaceAll("^/+", "").replaceAll("/+$", "");
                    String encodedKey = encodePathSegments(key);
                    String fullUrl = "https://kr.object.ncloudstorage.com/" + bucket + "/" + encodedKey; // path-style
                    return new ImageUrl(imageType, relatedId, fullUrl, imageSizeType);
                })
                .toList();

        imageRepository.saveAll(imageUrls);
    }

    private static String encodePathSegments(String path) {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(encode(parts[i], UTF_8).replace("+", "%20"));
        }
        return sb.toString();
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
        final String baseKey = UrlUtil.toKeyFromUrlOrKey(bucket, fileLocation);

        final String key = UrlUtil.joinKey(baseKey, fileName);

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key) // ← 최종 확정된 key
                    .build());
        } catch (SdkException e) {
            throw new RuntimeException("객체 삭제 실패(key=" + key + "): " + e.getMessage(), e);
        }
    }

    /**
     * ✅ 폴더 삭제 (prefix 기준)
     */
    @Override
    public void deleteFolder(String fileLocation) {
        String locKey = UrlUtil.toKeyFromUrlOrKey(bucket, fileLocation);
        String base = UrlUtil.firstNSegments(locKey, 2);
        String prefix = base.endsWith("/") ? base : base + "/";

        String continuation = null;
        try {
            do {
                var reqBuilder = ListObjectsV2Request.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .delimiter("/");

                if (continuation != null) {
                    reqBuilder.continuationToken(continuation);
                }

                var res = s3Client.listObjectsV2(reqBuilder.build());

                var toDelete = res.contents().stream()
                        .filter(o -> !o.key().endsWith("/"))
                        .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                        .collect(Collectors.toList());

                if (!toDelete.isEmpty()) {
                    var delReq = DeleteObjectsRequest.builder()
                            .bucket(bucket)
                            .delete(Delete.builder().objects(toDelete).build())
                            .build();
                    s3Client.deleteObjects(delReq);
                }

                continuation = res.isTruncated() ? res.nextContinuationToken() : null;
            } while (continuation != null);

        } catch (SdkException e) {
            throw new RuntimeException("폴더(1 depth) 삭제 실패: " + e.getMessage(), e);
        }
    }
}