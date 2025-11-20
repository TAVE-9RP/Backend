# Java 17 기반 이미지 사용
FROM eclipse-temurin:17-jdk

# JAR 파일 복사 (빌드시 이름 고정 또는 빌드 후 app.jar로 rename)
COPY app.jar app.jar

# 포트 오픈
EXPOSE 3006

# 서버 시간대 설정 변경
ENV TZ=Asia/Seoul
ENV LANG=C.UTF-8

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]