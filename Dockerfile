# -------- 1단계: Build Stage --------
FROM gradle:8.4-jdk17 AS builder

WORKDIR /app

# 소스 코드 복사 (Gradle Wrapper 포함)
COPY . .

# JAR 빌드
RUN gradle bootJar --no-daemon

# -------- 2단계: Runtime Stage --------
FROM openjdk:17-jdk-slim

VOLUME /tmp
WORKDIR /app

# builder 단계에서 만든 JAR만 복사
COPY --from=builder /app/deploy/build/libs/nighttrip-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
