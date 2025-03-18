import json
import logging
import joblib
from .models import PaymentCheck, USER
from django.utils.timezone import now
import numpy as np
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .utils import process_data, calculate_mse_and_anomaly, autoencoder
import os
from django.conf import settings
import redis
import requests

# Redis 설정
redis_client = redis.StrictRedis(host='localhost', port=6379, db=0, decode_responses=True)
MAX_REQUESTS = 100
TIME_WINDOW = 3600  # 1시간

# 로그 설정
logger = logging.getLogger(__name__)

# Encoder, Scaler 로드
# try:
encoder_path = os.path.join(settings.BASE_DIR, 'payment', 'asset', 'encoder.pkl')
scaler_path = os.path.join(settings.BASE_DIR, 'payment', 'asset', 'scaler.pkl')
x_val_path = os.path.join(settings.BASE_DIR, 'payment', 'asset', 'X_val.npy')

encoder = joblib.load(encoder_path)
scaler = joblib.load(scaler_path)
X_val = np.load(x_val_path)
# except Exception as e:
    # logger.error(f"파일 로드 오류: {str(e)}")
    # raise RuntimeError("필수 파일을 로드할 수 없습니다.") from e


def get_public_ip():
    try:
        response = requests.get('https://api.ipify.org?format=json')
        return response.json().get('ip')
    except requests.RequestException:
        return None


# 공인 IP 가져오기
public_ip = get_public_ip()
print(f"Your public IP is: {public_ip}")


def get_client_ip(request):
    x_forwarded_for = request.META.get('HTTP_X_FORWARDED_FOR')
    if x_forwarded_for:
        ip = x_forwarded_for.split(',')[0]
    else:
        ip = request.META.get('REMOTE_ADDR')

    if ip == "127.0.0.1":
        ip = get_public_ip()
    return ip


@csrf_exempt
def detect_fraud(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body.decode('utf-8'))

            print(data)  # 데이터 출력
            # 'userNum'을 포함한 데이터에서 값 가져오기
            userNum = data.get('userNum')

            if not userNum:
                return JsonResponse({"error": "userNum is missing"}, status=400)

            # userNum을 정수로 변환
            try:
                userNum = int(userNum)
            except ValueError:
                return JsonResponse({"error": "Invalid userNum format"}, status=400)

            # 클라이언트 IP 가져오기
            client_ip = get_client_ip(request)
            if not client_ip:
                return JsonResponse({"error": "IP address is missing"}, status=400)

            # IP 차단 체크
            request_count = redis_client.get(client_ip)
            if request_count:
                request_count = int(request_count)
                if request_count >= MAX_REQUESTS:
                    return JsonResponse({"error": "Too many requests, IP blocked"}, status=403)
            else:
                redis_client.setex(client_ip, TIME_WINDOW, 1)

            redis_client.incr(client_ip)

            # 데이터 전처리
            processed_data = process_data([data], encoder, scaler)

            # MSE 및 이상 거래 확률 계산
            threshold = 0.01
            dummy_mse, normal_above_threshold, anomaly_probability = calculate_mse_and_anomaly(
                processed_data, X_val, autoencoder, threshold
            )

            is_anomaly = (anomaly_probability * 100) > 50

            # USER 인스턴스를 가져와서 userNum에 할당
            try:
                user_instance = USER.objects.get(userNum=userNum)
            except USER.DoesNotExist:
                return JsonResponse({"error": "USER not found"}, status=404)

            # DB 저장 (user_ip 추가)
            PaymentCheck.objects.create(
                mse=round(dummy_mse, 4),
                normalAboveThreshold=round(normal_above_threshold * 100, 2),
                anomalyProbability=round(anomaly_probability * 100, 2),
                isAnomaly=is_anomaly,
                userIP=client_ip,  # IP 저장
                userNum=user_instance  # userNum을 USER 인스턴스로 저장
            )

            response_data = {
                "userNum": userNum,  # 올바르게 수정
                "mse": round(dummy_mse, 4),
                "normal_above_threshold": round(normal_above_threshold * 100, 2),
                "anomaly_probability": round(anomaly_probability * 100, 2),
                "is_anomaly": is_anomaly
            }

            return JsonResponse(response_data, status=200)

        except json.JSONDecodeError:
            return JsonResponse({"error": "Invalid JSON format"}, status=400)
        except ValueError as e:
            return JsonResponse({"error": f"Invalid value: {str(e)}"}, status=400)
        except Exception as e:
            logger.error(f"Unexpected error: {str(e)}")
            return JsonResponse({"error": "Internal Server Error"}, status=500)

    return JsonResponse({"message": "Only POST requests are allowed"}, status=405)
