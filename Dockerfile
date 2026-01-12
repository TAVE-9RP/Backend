# Java 17 기반 이미지 사용
FROM eclipse-temurin:17-jdk

COPY build/libs/*.jar app.jar

# 포트 오픈
EXPOSE 3006

# 서버 시간대 설정 변경
ENV TZ=Asia/Seoul
ENV LANG=C.UTF-8

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]