# 기존 Elasticsearch 8.12.0 이미지를 베이스로 사용
FROM elasticsearch:8.12.0

# Dockerfile이 있는 현재 디렉토리의 entrypoint.sh 파일을 컨테이너 내부로 복사
# 이때 컨테이너 내부에서는 자동으로 실행 권한이 부여됩니다.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

# create_index.json 파일을 컨테이너 내부의 Elasticsearch 설정 디렉토리로 복사
COPY create_index.json /usr/share/elasticsearch/config/create_index.json

# 컨테이너 시작 시 실행될 엔트리포인트 스크립트 지정
# 이 스크립트가 플러그인 설치, Elasticsearch 실행, 인덱스 생성 등을 담당합니다.
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

# Elasticsearch의 기본 CMD를 제거하여 ENTRYPOINT가 단독으로 실행되도록 함
CMD []