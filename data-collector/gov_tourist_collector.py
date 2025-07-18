import requests
import psycopg2
import time
import pandas as pd

from config import DB_CONFIG, TOUR_API_CONFIG

# ✅ 서비스 키 설정
SERVICE_KEY = TOUR_API_CONFIG["service_key"]

# ✅ 시군구 엑셀 파일에서 읽어오기
excel_df = pd.read_excel("area_code.xlsx")  # 또는 절대 경로 사용

SIGUNGU_LIST = [
    (int(row["areaCd"]), int(row["sigunguCd"]), row["sigunguNm"])
    for _, row in excel_df.iterrows()
]

def fetch_tourist_spots(area_code, sigungu_code, page_no=1, num_of_rows=100):
    url = "http://apis.data.go.kr/B551011/LocgoHubTarService1/areaBasedList1"
    params = {
        "serviceKey": SERVICE_KEY,
        "pageNo": page_no,
        "numOfRows": num_of_rows,
        "MobileOS": "WEB",
        "MobileApp": "nighttrip",
        "baseYm": "202406",
        "areaCd": area_code,
        "signguCd": sigungu_code,
        "_type": "json",
    }

    res = requests.get(url, params=params)
    print(res.headers.get("Content-Type"))
    print(res.text[:500])

    if res.status_code != 200:
        print(f"❌ API 실패 (areaCd: {area_code}, sigunguCd: {sigungu_code})")
        return [], 0

    try:
        data = res.json()
        body = data.get("response", {}).get("body", {})
        items = body.get("items", {})
        total_count = body.get("totalCount", 0)

        # 빈 문자열 처리
        if isinstance(items, str):
            return [], total_count

        item_list = items.get("item", [])
        item_list = item_list if isinstance(item_list, list) else [item_list]

        return item_list, total_count
    except Exception as e:
        print(f"❌ JSON 파싱 실패: {e}")
        return [], 0

def save_spot_to_db(item):
    try:
        # 필수 필드 검증
        if not all([
            item.get("hubTatsNm"),
            item.get("mapX"),
            item.get("mapY"),
            item.get("areaNm"),
            item.get("signguNm"),
        ]):
            print(f"⚠️ 필수 필드 누락: {item}")
            return

        spot_name = item.get("hubTatsNm")
        longitude = float(item.get("mapX"))
        latitude = float(item.get("mapY"))
        city_name = f"{item.get('areaNm')} {item.get('signguNm')}"
        category = item.get("hubCtgryMclsNm")  # 중분류 카테고리

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


def collect_gov_tourist_data():
    for area_code, sigungu_code, city_name in SIGUNGU_LIST:
        try:
            print(f"\n🏙️ 처리 중: {city_name} (areaCd: {area_code}, sigunguCd: {sigungu_code})")
            page = 1
            total = 999999

            while (page - 1) * 1000 < total:
                items, total = fetch_tourist_spots(area_code, sigungu_code, page)
                if not items:
                    break
                for item in items:
                    save_spot_to_db(item)
                print(f"📄 페이지 {page} 처리 완료 (가져온 개수: {len(items)})")
                page += 1
                time.sleep(0.3)

        except Exception as e:
            print(f"❌ 처리 실패: {city_name}, 에러: {e}")

if __name__ == "__main__":
    collect_gov_tourist_data()
