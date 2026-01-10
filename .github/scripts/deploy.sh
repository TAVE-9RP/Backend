#!/bin/bash

HOST=$EC2_HOST
USER=$EC2_USER
KEY_PATH="key.pem"

echo "$EC2_SSH_KEY" > $KEY_PATH
chmod 600 $KEY_PATH

# .env 파일 생성 (Secrets에 저장된 전체 내용을 전달)
# 이 단계가 있어야 docker run 시 --env-file이 작동합니다.
echo "${ENV_FILE}" > .env

# EC2 서버로 .env 파일 전송
scp -i $KEY_PATH .env $USER@$HOST:/home/$USER/nexerp/

ssh -i $KEY_PATH $USER@$HOST <<EOF
  # 1. ECR 로그인
  aws ecr get-login-password --region ap-southeast-2 | docker login --username AWS --password-stdin ${ECR_REGISTRY_URL}

  # 2. 기존 컨테이너 중지 및 제거
  docker stop nexerp-container || true
  docker rm nexerp-container || true

  # 3. 새로운 이미지 가져오기
  docker pull ${ECR_REGISTRY_URL}/nexerp-server:latest

  # 4. 컨테이너 실행 (환경변수 명시적 주입으로 ReadOnly DB 이슈 방지)
  docker run -d --name nexerp-container \
    -p 3006:3006 \
    --env SPRING_PROFILES_ACTIVE=prod \
    --env-file /home/$USER/nexerp/.env \
    ${ECR_REGISTRY_URL}/nexerp-server:latest

  # 5. 사용하지 않는 오래된 이미지 삭제 (디스크 용량 관리)
  docker image prune -f
EOF