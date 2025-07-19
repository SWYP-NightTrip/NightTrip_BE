import requests
import urllib.request
import urllib.parse
import psycopg2
import time
import pandas as pd
import json

from config import DB_CONFIG, TOUR_API_CONFIG, NAVER_MAP_CONFIG

SERVICE_KEY = TOUR_API_CONFIG["service_key"]

excel_df = pd.read_excel("area_code.xlsx")
SIGUNGU_LIST = [
    (int(row["areaCd"]), int(row["sigunguCd"]), row["sigunguNm"])
    for _, row in excel_df.iterrows()
]

def get_coordinates_from_naver(query, NAVER_MAP_CONFIG):
    client_id = NAVER_MAP_CONFIG['client_id']
    client_secret = NAVER_MAP_CONFIG['client_secret']
    enc_query = urllib.parse.quote(query)

    url = f"https://openapi.naver.com/v1/search/local.json?query={enc_query}&display=1&sort=random"
    request = urllib.request.Request(url)
    request.add_header("X-Naver-Client-Id", client_id)
    request.add_header("X-Naver-Client-Secret", client_secret)

    try:
        response = urllib.request.urlopen(request)
        if response.getcode() == 200:
            data = json.loads(response.read().decode('utf-8'))
            items = data.get("items", [])
            if not items:
                print(f"⚠️ 네이버 검색 결과 없음: {query}")
                return None
            item = items[0]
            latitude = float(item["mapy"]) / 1e7
            longitude = float(item["mapx"]) / 1e7
            return latitude, longitude
    except Exception as e:
        print(f"❌ 네이버 API 오류 ({query}): {e}")
    return None


def fetch_related_keywords(base_ym, area_code, sigungu_code):
    url = "http://apis.data.go.kr/B551011/TarRlteTarService1/areaBasedList1"
    params = {
        "serviceKey": SERVICE_KEY,
        "pageNo": 1,
        "numOfRows": 100,
        "MobileOS": "WEB",
        "MobileApp": "nighttrip",
        "_type": "json",
        "baseYm": base_ym,
        "areaCd": area_code,
        "signguCd": sigungu_code
    }

    try:
        res = requests.get(url, params=params)
        res.raise_for_status()
        data = res.json()
        items = data.get("response", {}).get("body", {}).get("items", {}).get("item", [])
        if isinstance(items, dict):
            items = [items]
        if not isinstance(items, list):
            print(f"❌ 예상 외 응답 형식: {items}")
            return []
        return items  # ✅ dict 목록 그대로 반환
    except Exception as e:
        print(f"❌ 연관 키워드 조회 실패: {e}")
        return []

def fetch_spots_by_keyword(keyword, base_ym, area_code, sigungu_code):
    url = "http://apis.data.go.kr/B551011/TarRlteTarService1/searchKeyword1"
    params = {
        "serviceKey": SERVICE_KEY,
        "pageNo": 1,
        "numOfRows": 100,
        "MobileOS": "WEB",
        "MobileApp": "nighttrip",
        "_type": "json",
        "baseYm": base_ym,
        "areaCd": area_code,
        "signguCd": sigungu_code,
        "keyword": keyword
    }

    try:
        res = requests.get(url, params=params)
        res.raise_for_status()
        data = res.json()
        items = data.get("response", {}).get("body", {}).get("items", {}).get("item", [])

        if isinstance(items, dict):
            items = [items]
        if not isinstance(items, list):
            print(f"❌ 예상 외 응답 형식: {items}")
            return []

        if not items:
            print(f"⚠️ 키워드 '{keyword}'로 조회된 관광지가 없습니다.")
            return []

        return items
    except Exception as e:
        print(f"❌ 키워드로 관광지 조회 실패 ({keyword}): {e}")
        return []

def save_spot_to_db(item):
    try:
        spot_name = item.get("rlteTatsNm")
        regn_nm = item.get("rlteRegnNm")
        signgu_nm = item.get("rlteSignguNm")

        if not all([spot_name, regn_nm, signgu_nm]):
            print(f"⚠️ 필수 필드 누락 (spot_name, regn, signgu): {item}")
            return

        city_name = f"{regn_nm} {signgu_nm}"
        category = item.get("rlteCtgryMclsNm") or None  # 키 없으면 None

        coords = get_coordinates_from_naver(city_name+spot_name+" ", NAVER_MAP_CONFIG)
        if coords is None:
            print(f"⚠️ 위치 정보 조회 실패: {spot_name}")
            return
        latitude, longitude = coords

        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()

        cur.execute("""
            INSERT INTO tourist_spot (spot_name, longitude, latitude, city_id, category)
            SELECT %s, %s, %s, city.city_id, %s
            FROM city
            WHERE city.city_name = %s
            ON CONFLICT (city_id, spot_name) DO NOTHING;
        """, (
            spot_name,
            longitude,
            latitude,
            category,
            city_name
        ))

        conn.commit()
        print(f"📝 저장 완료: {spot_name}")

    except Exception as e:
        print(f"❌ DB 저장 실패: {e}")
    finally:
        if 'cur' in locals():
            cur.close()
        if 'conn' in locals():
            conn.close()



def collect_related_tourist_data():
    base_ym = "202504"
    for area_code, sigungu_code, sigungu_name in SIGUNGU_LIST:
        print(f"🔍 {sigungu_name} 기준 연관 관광지명 키워드 수집 중...")
        keywords = fetch_related_keywords(base_ym, area_code, sigungu_code)
        for keyword_obj in keywords:
            save_spot_to_db(keyword_obj)  # dict 직접 넘김
            spot_name = keyword_obj.get("rlteTatsNm")
            if not spot_name:
                continue
            items = fetch_spots_by_keyword(spot_name, base_ym, area_code, sigungu_code)
            for item in items:
                save_spot_to_db(item)

if __name__ == "__main__":
    collect_related_tourist_data()
