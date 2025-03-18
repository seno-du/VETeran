from django.db import models


# USER 모델 정의
class USER(models.Model):
    userNum = models.AutoField(primary_key=True)  # 사용자 고유번호 (PK)
    userName = models.CharField(max_length=50)  # 사용자 이름
    userId = models.CharField(max_length=50, unique=True)  # 로그인 ID (유니크)
    userPwd = models.CharField(max_length=100)  # 비밀번호
    userPhone = models.CharField(max_length=15, unique=True)  # 연락처 (유니크)
    userBirth = models.DateField()  # 생년월일
    userEmail = models.EmailField(max_length=100, unique=True)  # 이메일 (유니크)
    userAddress = models.CharField(max_length=100)  # 주소
    userAddressNum = models.CharField(max_length=5)  # 우편번호
    userStatus = models.CharField(max_length=10, choices=[('활성', '활성'), ('비활성', '비활성')], default='활성')  # 상태
    userSignupDate = models.DateTimeField(auto_now_add=True)  # 가입일시

    class Meta:
        db_table = 'USER'


# PaymentCheck 모델 정의
class PaymentCheck(models.Model):
    mse = models.FloatField()
    normalAboveThreshold = models.FloatField()
    anomalyProbability = models.FloatField()
    isAnomaly = models.BooleanField()
    userIP = models.CharField(max_length=200)
    createdAt = models.DateTimeField(auto_now_add=True)
    userNum = models.ForeignKey('USER', on_delete=models.CASCADE, db_column='userNum')  # 컬럼 이름 명시적으로 지정

    class Meta:
        db_table = 'PAYMENTCHECK'

