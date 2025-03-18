import json
import time
import datetime
import uuid
import hmac
import hashlib
import requests
import platform
import random
from dotenv import load_dotenv
import os

load_dotenv()
coolSMS = os.getenv("COOLAPI_KEY"),os.getenv("COOLAPI_SECRETKEY")

# 아래 값은 필요시 수정
protocol = 'https'
domain = 'api.coolsms.co.kr'
prefix = ''

def unique_id():
    return str(uuid.uuid1().hex)

def get_iso_datetime():
    utc_offset_sec = time.altzone if time.localtime().tm_isdst else time.timezone
    utc_offset = datetime.timedelta(seconds=-utc_offset_sec)
    return datetime.datetime.now().replace(tzinfo=datetime.timezone(offset=utc_offset)).isoformat()


def get_signature(key, msg):
    return hmac.new(key.encode(), msg.encode(), hashlib.sha256).hexdigest()


def get_headers(api_key, api_secret):
    date = get_iso_datetime()
    salt = unique_id()
    combined_string = date + salt

    return {
        'Authorization': 'HMAC-SHA256 ApiKey=' + api_key + ', Date=' + date + ', salt=' + salt + ', signature=' +
                         get_signature(api_secret, combined_string),
        'Content-Type': 'application/json; charset=utf-8'
    }


def get_url(path):
    url = '%s://%s' % (protocol, domain)
    if prefix != '':
        url += prefix
    url = url + path
    return url

def send_many(parameter):
    api_key = os.getenv("COOLAPI_KEY")
    api_secret = os.getenv("COOLAPI_SECRETKEY")
    parameter['agent'] = {
        'sdkVersion': 'python/4.2.0',
        'osPlatform': platform.platform() + " | " + platform.python_version()
    }

    return requests.post(get_url('/messages/v4/send-many'), headers=get_headers(api_key, api_secret), json=parameter)

# 예약 문자 전송 함수
def send_reservation_result(phone_numbers, reservation_status):
    # 상태에 따른 메시지 설정
    if reservation_status == "완료":
        status_message = "예약이 완료되었습니다."
    elif reservation_status == "취소":
        status_message = "예약이 취소되었습니다."
    else:
        status_message = "예약 상태가 변경되었습니다."

    # 전화번호가 여러 명일 때
    if isinstance(phone_numbers, list):
        print(f"Sending message to multiple phone numbers: {phone_numbers}")
        data = {
            'messages': [
                {
                    'to': phone_number,
                    'from': os.getenv("FROM_PHONENUMBER"),
                    'text': f'귀하의 예약 결과: {status_message}'
                }
                for phone_number in phone_numbers  # 여러 전화번호에 대한 메시지
            ]
        }
    else:  # 전화번호가 한 명일 때
        print(f"Sending message to single phone number: {phone_numbers}")
        data = {
            'messages': [
                {
                    'to': phone_numbers,
                    'from': os.getenv("FROM_PHONENUMBER"),
                    'text': f'귀하의 예약 결과: {status_message}'
                }
            ]
        }

    # 문자 전송
    res = send_many(data)
    return res


# 인증번호 생성
def generate_verification_code():
    arr = []
    for i in range(1, 7):
        rand = random.randint(0,9)
        arr.append(rand)
    arr = list(map(str, arr))
    return "".join(arr)

def send_message(phone_number, verification_code):
    data = {
        'messages': [
            {
                'to': phone_number,
                'from': os.getenv("FROM_PHONENUMBER"),
                'text': f'본인확인 인증번호 {verification_code} 를 입력해 주세요'
            },
                    ]
    }
    res = send_many(data)
    return res

# if __name__ == '__main__':
#     data = {
#         'messages': [
#             {
#                 'to': '01085597310',
#                 'from': '01077554183',
#                 'text': f'본인확인 인증번호 {generate_verification_code()} 를 입력해 주세요'
#             },
#                     ]
#     }
#     res = send_many(data)
#     print(json.dumps(res.json(), indent=2, ensure_ascii=False))