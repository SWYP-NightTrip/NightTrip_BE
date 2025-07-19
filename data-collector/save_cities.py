import requests
import psycopg2
from config import DB_CONFIG, VWORLD_CONFIG

countries = ["Korea", "Japan", "France"]

def get_korean_sigungu_list():
    url = "https://api.vworld.kr/req/data"
    params = {
        "service": "data",
        "request": "GetFeature",
        "data": "LT_C_ADSIGG_INFO",
        "key": VWORLD_CONFIG["key"],  # ⚠️ 올바른 인증키
        "domain": VWORLD_CONFIG["domain"],
        "format": "json",
        "size": 1000,
        "geometry": "false",
        "attrFilter": "full_nm:like:%",
    }


    res = requests.get(url, params=params)
    res.raise_for_status()
    res_data = res.json()

    features = res_data.get("response", {}).get("result", {}).get("featureCollection", {}).get("features", [])
    result = []
    for f in features:
        props = f.get("properties", {})
        full_nm = props.get("full_nm")
        sig_cd = props.get("sig_cd")
        if full_nm and sig_cd:
            result.append((full_nm, int(sig_cd)))
    return result

def get_city_list_by_country(country):
    url = "https://countriesnow.space/api/v0.1/countries/cities"
    payload = {"country": country}
    headers = {"Content-Type": "application/json"}

    res = requests.post(url, json=payload, headers=headers)
    res_data = res.json()

    if res_data.get("error") is False:
        return [(city_name, None) for city_name in res_data.get("data", [])]

    else:
        print(f"❌ {country}의 도시 목록을 가져오는 데 실패했습니다: {res_data.get('msg')}")
        return []

def save_to_postgres(country_city_data):
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()

    for entry in country_city_data:
        country_name = entry["country"]
        cities_name = entry["cities"]

        for city in cities_name:
            if isinstance(city, tuple):  # 한국일 경우 (city_name, sigungu_code)
                city_name, sigungu_code = city
            else:  # 외국은 그냥 str
                city_name, sigungu_code = city, None

            cur.execute("""
                INSERT INTO city (country_name, city_name, sigungu_code)
                VALUES (%s, %s, %s)
                ON CONFLICT (country_name, city_name) DO NOTHING;
            """, (country_name, city_name, sigungu_code))


    conn.commit()
    cur.close()
    conn.close()
    print("✅ 저장 완료")

if __name__ == "__main__":
    collected_data = []

    for country in countries:
        if country == "Korea":
            cities = get_korean_sigungu_list()
        else:
            cities = get_city_list_by_country(country)

        if cities:
            collected_data.append({
                "country": country,
                "cities": cities
            })
        else:
            print(f"⚠️ {country} 도시 없음")

    save_to_postgres(collected_data)
