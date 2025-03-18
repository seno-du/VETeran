from django.http import JsonResponse, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from .utils import send_message, generate_verification_code, generate_jwt, send_reservation_result
import redis
import uuid
import json
from sqlalchemy import text
from database import get_db, RESERVE


r = redis.Redis()

@csrf_exempt
def phone_auth(request):
    try:
        # if request.method != "POST":
        #     return HttpResponse("Method Not Allowed", status=405)

        data = json.loads(request.body)

        phone_number = data["phone_number"]
        code = generate_verification_code()

        send_message(phone_number, code)

        uuuid = str(uuid.uuid4())
        r.set(uuuid, code, ex=300)

        token = generate_jwt(uuuid)
        print(token)
        print(code)
        return JsonResponse({
            'token': token
        })
    except Exception as e:
        print(e)
        return HttpResponse("알 수 없는 오류가 발생했습니다", status=500)

# 예약 상태에 맞는 문자 발송
@csrf_exempt
def send_reservation_status(request):
    try:
        data = json.loads(request.body)
        reserveNum = data.get("reserveNum")  # 예약 번호
        # 현재 예약 정보 가져오기
        db = next(get_db())
        reservation = db.query(RESERVE).filter(RESERVE.reserveNum == reserveNum).first()

        if not reservation:
            print("예약 정보가 없습니다.")
            return JsonResponse({"error": "예약 정보가 없습니다."}, status=404)

        sql = text("""
            SELECT user.userPhone 
            FROM USER user, PET pet, RESERVE reserve 
            WHERE reserve.petNum = pet.petNum 
            AND user.userNum = pet.userNum
            AND reserve.reserveNum = :reserveNum
        """)
        result = db.execute(sql, {'reserveNum': reserveNum})  # 예약 번호로 수정
        userPhones = [str(row[0]) for row in result.fetchall()]  # 여러 전화번호 추출
        
        if not userPhones:  # 전화번호가 없으면 에러 반환
            print(f"전화번호를 찾을 수 없습니다. 예약 번호: {reserveNum}")
            return JsonResponse({"error": "전화번호를 찾을 수 없습니다."}, status=404)

        print(f"userPhones: {userPhones}")

        # 상태에 맞는 문자 발송
        send_reservation_result(userPhones, reservation.reserveStatus)  # 여러 전화번호에 문자 보내기

        return JsonResponse({"message": f"예약 상태 '{reservation.reserveStatus}'에 대한 문자 전송이 완료되었습니다."})
    
    except Exception as e:
        print(e)
        return HttpResponse("알 수 없는 오류가 발생했습니다", status=500)

