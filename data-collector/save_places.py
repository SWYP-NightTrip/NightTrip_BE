from config import DB_CONFIG, NAVER_MAP_CONFIG, KAKAO_MAP_CONFIG
import psycopg2
import urllib.request
import urllib.parse
import json
import requests
from key_words import keywords
import time

def fetch_cities_from_db():
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    cur.execute("SELECT city_name FROM city WHERE country_name = %s;", ('Korea',))
    rows = cur.fetchall()
    cur.close()
    conn.close()
    print(f"📋 총 {len(rows)}개의 도시 불러옴")
    return [row[0] for row in rows]

def search_place_naver(query, display=5):
    client_id = NAVER_MAP_CONFIG['client_id']
    client_secret = NAVER_MAP_CONFIG['client_secret']
    encText = urllib.parse.quote(query)

    sort_modes = ["random", "comment"]
    all_results = []

    for sort in sort_modes:
        url = f"https://openapi.naver.com/v1/search/local.json?query={encText}&display={display}&sort={sort}"
        request = urllib.request.Request(url)
        request.add_header("X-Naver-Client-Id", client_id)
        request.add_header("X-Naver-Client-Secret", client_secret)

        try:
            response = urllib.request.urlopen(request)
            if response.getcode() == 200:
                data = json.loads(response.read().decode('utf-8'))
                items = data.get("items", [])
                for item in items:
                    try:
                        result = {
                            "title": item["title"],
                            "latitude": float(item["mapy"]) / 1e7,
                            "longitude": float(item["mapx"]) / 1e7,
                            "address": item["address"]
                        }
                        all_results.append(result)
                    except (KeyError, ValueError):
                        continue
        except Exception as e:
            print(f"❌ {sort.upper()} 정렬 API 요청 실패: {e}")

    # 중복 제거 (제목 기준)
    seen_titles = set()
    unique_results = []
    for r in all_results:
        if r["title"] not in seen_titles:
            unique_results.append(r)
            seen_titles.add(r["title"])

    return unique_results


# def search_place_kakao(query, size=5):
#     kakao_api_key = KAKAO_MAP_CONFIG['rest_api_key']
#     headers = {"Authorization": f"KakaoAK {kakao_api_key}"}
#     url = "https://dapi.kakao.com/v2/local/search/keyword.json"
#     params = {
#         "query": query,
#         "size": size,
#         "sort": "accuracy",  # 또는 'distance'
#     }
#
#     try:
#         res = requests.get(url, headers=headers, params=params)
#         if res.status_code != 200:
#             print(f"❌ KAKAO API 실패: {query}, 상태코드 {res.status_code}")
#             return []
#
#         data = res.json()
#         places = data.get("documents", [])
#         result = []
#         for p in places:
#             try:
#                 result.append({
#                     "title": p["place_name"],
#                     "latitude": float(p["y"]),
#                     "longitude": float(p["x"]),
#                     "address": p.get("address_name", ""),
#                 })
#             except (KeyError, ValueError):
#                 continue
#         return result
#
#     except Exception as e:
#         print(f"❌ KAKAO 요청 에러: {e}")
#         return []

def search_places_combined(city, keyword, display=5):
    query = f"{city} {keyword}"
    naver_results = search_place_naver(query, display=display)
#     kakao_results = search_place_kakao(query, size=display)

    combined = naver_results

    # 제목 기준 중복 제거
    seen = set()
    unique = []
    for item in combined:
        if item["title"] not in seen:
            unique.append(item)
            seen.add(item["title"])
    return unique


def save_place_to_db(city_name, keyword, place_data):
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()

    cur.execute("SELECT city_id FROM city WHERE city_name = %s;", (city_name,))
    city_row = cur.fetchone()
    if not city_row:
        cur.close()
        conn.close()
        return
    city_id = city_row[0]

    try:
        cur.execute("""
            INSERT INTO tourist_spot (spot_name, longitude, latitude, city_id)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT ON CONSTRAINT tourist_spot_city_id_spot_name_key DO NOTHING;
        """, (
            place_data["title"],
            place_data["longitude"],
            place_data["latitude"],
            city_id
        ))
        conn.commit()
    except Exception as e:
        print(f"❌ INSERT 실패: {e}")
    finally:
        cur.close()
        conn.close()

if __name__ == "__main__":
    start_time = time.time()

    cities = fetch_cities_from_db()

    for city in cities:
        for keyword in keywords:
            places = search_places_combined(city, keyword)
            if places:
                for place in places:
                    save_place_to_db(city, keyword, place)
            else:
                print(f"❌ 장소 검색 실패: {city} {keyword}")

    elapsed = time.time() - start_time
    print(f"\n✅ 모든 작업 완료 - 총 소요 시간: {elapsed:.2f}초")

