from django.shortcuts import render
import os
import numpy as np
from django.conf import settings
from django.http import JsonResponse
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from django.views.decorators.csrf import csrf_exempt
from aop import aspect

# 모델 로드
MODEL_PATHS = {
    "mu": os.path.join(settings.BASE_DIR, 'diagnosis', 'asset', 'Mu.keras'),
    "ch": os.path.join(settings.BASE_DIR, 'diagnosis', 'asset', 'Ch.keras')
}

MODELS = {key: load_model(path) for key, path in MODEL_PATHS.items()}

# 클래스 매핑
RESULT_MAP = {
    "ch": ["ABN", "NOR"],
    "mu": ['Mu05', 'Mu06', 'Mu07', 'NOR']
}

# 이미지 전처리
def preprocess_image(image_path, target_size=(384, 384)):
    img = load_img(image_path, target_size=target_size, color_mode='grayscale')
    img_array = img_to_array(img) / 255.0
    return np.expand_dims(img_array, axis=0)

# 진단 예측
@csrf_exempt
@aspect.apply
def predict_diagnosis(request):
    if request.method != 'POST' or 'file' not in request.FILES:
        return JsonResponse({'error': 'Invalid request'}, status=400)

    img_file = request.FILES['file']
    diagnosis_type = request.POST.get('diagnosis_type')

    if diagnosis_type not in MODELS:
        return JsonResponse({'error': 'Invalid diagnosis type'}, status=400)

    try:
        # 임시 파일 저장
        img_path = os.path.join(settings.MEDIA_ROOT, 'uploads', img_file.name)
        os.makedirs(os.path.dirname(img_path), exist_ok=True)
        with open(img_path, 'wb') as f:
            for chunk in img_file.chunks():
                f.write(chunk)

        # 이미지 전처리 및 예측
        img_array = preprocess_image(img_path)
        predictions = MODELS[diagnosis_type].predict(img_array)
        print(predictions)
        if len(RESULT_MAP[diagnosis_type]) == 2:
            predicted_label = "NOR" if predictions[0][0] < 0.5 else "ABN"
        else :
            predicted_index = np.argmax(predictions, axis=1)[0]
            predicted_label = RESULT_MAP[diagnosis_type][predicted_index]

        return JsonResponse({'result': predicted_label})

    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)