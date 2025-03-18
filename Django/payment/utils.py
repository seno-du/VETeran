# utils.py
import numpy as np
import pandas as pd
from tensorflow.keras.models import load_model
import os
from django.conf import settings

# 올바른 경로 설정 (Django 한 번만 포함)
model_path = os.path.join(settings.BASE_DIR, 'payment', 'asset', 'autoencoder.keras')

# 모델 로드
autoencoder = load_model(model_path)

# 전처리 함수
def process_data(data, encoder, scaler):
    # DataFrame로 변환
    data_df = pd.DataFrame(data)
    
    data_df['결제일시'] = (pd.to_datetime("today") - pd.to_datetime(data_df["결제일시"])).dt.days

    # 범주형 데이터 원-핫 인코딩
    encoded_cols_dummy = encoder.transform(data_df[["거래내용", "거래방법"]])
    encoded_data_df = pd.DataFrame(encoded_cols_dummy, columns=encoder.get_feature_names_out(["거래내용", "거래방법"]))

    # 최종 데이터프레임 구성
    data_final = pd.concat([data_df[["금액", "해외거래", "결제일시"]], encoded_data_df], axis=1)

    # 정규화 (StandardScaler 사용)
    data_scaled = scaler.transform(data_final)
    data_scaled_df = pd.DataFrame(data_scaled, columns=data_final.columns)

    return data_scaled_df


# MSE와 비정상 거래 확률 계산
def calculate_mse_and_anomaly(dummy_scaled_df, X_val, autoencoder, threshold):
    # 데이터의 MSE 계산
    dummy_reconstructed = autoencoder.predict(dummy_scaled_df)
    dummy_mse = np.mean(np.square(dummy_scaled_df - dummy_reconstructed))

    # 정상 거래의 MSE 계산 (전체 정상 거래 데이터에서)
    normal_reconstructed = autoencoder.predict(X_val)
    normal_mse = np.mean(np.square(X_val - normal_reconstructed), axis=1)

    # 임계값을 초과하는 비율 확인
    normal_above_threshold = np.sum(normal_mse > threshold) / len(normal_mse)

    # 데이터가 임계값을 초과하는지 확인
    is_above_threshold = dummy_mse > threshold

    # 비정상 거래일 확률 (임계값을 넘을 확률)
    anomaly_probability = normal_above_threshold if is_above_threshold else 0

    return dummy_mse, normal_above_threshold, anomaly_probability


# ---------------------------------------- IP ----------------------------------------
def is_valid_ip(ip):
    ip_parts = ip.split('.')
    if len(ip_parts) != 4:
        return False
    for part in ip_parts:
        if not part.isdigit() or not (0 <= int(part) <= 255):
            return False
    return True


def check_ip_in_range(ip, start_ip, end_ip):
    if not (is_valid_ip(ip) and is_valid_ip(start_ip) and is_valid_ip(end_ip)):
        return False

    ip_parts = list(map(int, ip.split('.')))
    start_parts = list(map(int, start_ip.split('.')))
    end_parts = list(map(int, end_ip.split('.')))

    return all(start_parts[i] <= ip_parts[i] <= end_parts[i] for i in range(4))


def check_ip_in_sdf(ip_to_check):
    # CSV 파일 로드
    sdf = pd.read_csv("payment/asset/국내IP주소전처리완료.csv")

    # 각 행에 대해 IP 범위 체크
    return sdf.apply(lambda row: check_ip_in_range(ip_to_check, row['시작주소'], row['끝주소']), axis=1).any()
