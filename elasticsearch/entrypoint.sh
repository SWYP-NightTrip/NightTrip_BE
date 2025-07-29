#!/bin/bash

# 1. Nori 플러그인 설치
if [ ! -f /usr/share/elasticsearch/plugins/nori/plugin-descriptor.properties ]; then
  echo 'Installing Nori plugin...'
  elasticsearch-plugin install --batch analysis-nori;
else
  echo 'Nori plugin already installed.'
fi;

# 2. Elasticsearch 서비스 백그라운드로 시작
echo 'Starting Elasticsearch in background...'
/usr/local/bin/docker-entrypoint.sh elasticsearch &
ELASTICSEARCH_PID=$! # Elasticsearch 프로세스 ID 저장

# 3. Elasticsearch가 green 상태가 될 때까지 기다림
ELASTICSEARCH_URL='http://localhost:9200'
INDEX_NAME='search_documents'
ATTEMPTS=0
MAX_ATTEMPTS=60 # 최대 5분 대기

echo 'Waiting for Elasticsearch to be ready and green...'
until curl --silent $ELASTICSEARCH_URL/_cluster/health?wait_for_status=green 2>/dev/null | grep -q '\"status\":\"green\"'; do
  echo 'Elasticsearch is not yet green. Retrying in 5 seconds...'
  sleep 5
  ATTEMPTS=$((ATTEMPTS+1))
  if [ $ATTEMPTS -ge $MAX_ATTEMPTS ]; then
    echo 'Max attempts reached. Elasticsearch did not become ready for index creation.'
    kill $ELASTICSEARCH_PID # Elasticsearch 프로세스 종료
    exit 1 # 스크립트 실패
  fi
done
echo 'Elasticsearch is ready and green! Proceeding with index creation...'

# 4. 인덱스 존재 여부 확인 및 삭제 (개발 시 초기화를 위해 유용)
# 프로덕션에서는 이 줄을 신중하게 사용하거나 제거해야 합니다.
if curl --silent --fail $ELASTICSEARCH_URL/$INDEX_NAME -o /dev/null; then
  echo 'Index exists. Deleting existing index...'
  curl -X DELETE $ELASTICSEARCH_URL/$INDEX_NAME -H 'Content-Type: application/json'
  echo 'Index deleted.'
else
  echo 'Index does not exist. Proceeding with creation.'
fi

# 5. 인덱스 생성
echo 'Creating Elasticsearch index with mapping...'
curl -X PUT $ELASTICSEARCH_URL/$INDEX_NAME \
     -H 'Content-Type: application/json' \
     --data-binary @/usr/share/elasticsearch/config/create_index.json

if [ $? -eq 0 ]; then
  echo 'Elasticsearch index created successfully!'
else
  echo 'Failed to create Elasticsearch index!'
  kill $ELASTICSEARCH_PID # Elasticsearch 프로세스 종료
  exit 1 # 스크립트 실패
fi

# 6. Elasticsearch 서비스가 계속 실행되도록 유지 (매우 중요!)
echo 'Elasticsearch initialization complete. Keeping Elasticsearch service alive...'
wait $ELASTICSEARCH_PID # 백그라운드에서 실행된 elasticsearch 프로세스가 종료될 때까지 기다림 (컨테이너가 종료되지 않도록 함)