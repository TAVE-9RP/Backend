#!/bin/bash

HOST=$EC2_HOST
USER=$EC2_USER
KEY_PATH="key.pem"

# PEM 키 복사 후 권한 설정
echo "$EC2_SSH_KEY" > $KEY_PATH
chmod 600 $KEY_PATH

# gradlew 실행 권한 부여 및 빌드
chmod +x ./gradlew
./gradlew bootJar

# JAR 복사
JAR_PATH=$(find build/libs -name "*.jar" | head -n 1)
cp "$JAR_PATH" app.jar

# EC2에 디렉토리 생성
ssh -i $KEY_PATH $USER@$HOST "mkdir -p ~/nexerp"

# JAR, Dockerfile, .env 전송
scp -i $KEY_PATH app.jar $USER@$HOST:/home/$USER/nexerp/
scp -i $KEY_PATH Dockerfile $USER@$HOST:/home/$USER/nexerp/
scp -i $KEY_PATH .env $USER@$HOST:/home/$USER/nexerp/

# EC2에서 컨테이너 재시작
ssh -i $KEY_PATH $USER@$HOST <<EOF
  cd ~/nexerp
  docker stop nexerp-container || true
  docker rm nexerp-container || true
  docker rmi nexerp || true

  docker build -t nexerp .
  docker run -d --name nexerp-container -p 3004:3004 --env-file .env nexerp
EOF
