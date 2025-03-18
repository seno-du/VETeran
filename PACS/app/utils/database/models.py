from .database import Base
from sqlalchemy import text
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Time, Enum, DECIMAL, Date
from sqlalchemy.dialects.mysql import LONGTEXT
import datetime

class MedicalFile(Base):
    __tablename__ = 'MEDICALFILE'
    medicalNum = Column(Integer, primary_key=True, autoincrement=True)  # 고유번호
    managerNum = Column(Integer, ForeignKey('MANAGER.managerNum', ondelete='CASCADE'), nullable=False)  # 등록자 번호
    petNum = Column(Integer, ForeignKey("PET.petNum", ondelete="CASCADE"), nullable=False)  # 환자(반려동물) 번호
    medicalDate = Column(DateTime, nullable=False)  # 입력한 날짜, 시간
    medicalNote = Column(String(1000))  # 해당 영상물에 대한 노트
    studyUid = Column(String(100))  # study_uid
    seriesUid = Column(String(100))  # series_uid
    instanceUid = Column(String(100))  # instance_uid
    studyTime = Column(Time)  # 촬영 날짜/시간
    accessionNumber = Column(String(50))  # 접수 번호
    studyId = Column(String(50))  # 검사 ID
    modalitiesInStudy = Column(String(50))  # 검사에 사용된 장비 종류
    seriesModality = Column(String(50))  # 시리즈 장비 종류
    seriesNumber = Column(Integer)  # 시리즈 번호
    seriesDescription = Column(String(200))  # 시리즈 설명
    modality = Column(String(50))  # 이미지 모달리티
    studyDate = Column(Date)  # 검사일
    manufacturer = Column(String(100))  # 장비 제조사
    manufacturerModelName = Column(String(100))  # 장비 모델명
    instanceNumber = Column(Integer, nullable=False)  # 이미지 번호
    frameOfReferenceUID = Column(String(100))  # 프레임 오브 레퍼런스 UID
    filePath = Column(String(200), nullable=False)  # 파일 경로
    fileSize = Column(Integer, nullable=False)  # 파일 크기
    studyDescription = Column(String(200))  # 연구 설명 (Dicom 표준 준수를 위해 추가)

class Pet(Base):
    __tablename__ = 'PET'
    petNum = Column(Integer, primary_key=True, autoincrement=True)  # 반려동물 고유번호 (PK)
    userNum = Column(Integer, ForeignKey('USER.userNum', ondelete='CASCADE'), nullable=False)  # 보호자(사용자) 고유번호 (FK)
    petSpecies = Column(Enum('강아지', '고양이', name='pet_species'), nullable=False)  # 동물 종
    petColor = Column(String(20), nullable=False)  # 동물 모색
    petName = Column(String(15), nullable=False)  # 반려동물 이름
    petBreed = Column(String(20), nullable=False)  # 반려동물 품종
    petGender = Column(Enum('암컷', '수컷', '중성화암컷', '중성화수컷', name='pet_gender'), nullable=False)  # 반려동물 성별
    petBirth = Column(Date, nullable=False)  # 반려동물 생년월일
    petMicrochip = Column(String(15), nullable=True)  # 반려동물 마이크로칩 번호
    petWeight = Column(DECIMAL(5, 2), nullable=False)  # 반려동물 체중 (Kg 단위)
    petStatus = Column(Enum('활성화', '비활성화', name='pet_status'), nullable=False, default='활성화')  # 반려동물 상태
    petImage = Column(String(255), nullable=True)  # 반려동물 이미지 경로
    
class Log(Base):
    __tablename__ = 'LOG'

    logNum = Column(Integer, primary_key=True, autoincrement=True)  # 로그 고유번호 (PK)
    logCategory = Column(Enum('INFO', 'ERROR', name='log_category'), nullable=False)  # 로그 카테고리
    logLocation = Column(String(20), nullable=False)  # 발생 위치
    logDetail = Column(LONGTEXT, nullable=False)  # 상세 로그 (stackTrace 등)
    logRemoteAddr = Column(String(20), nullable=True)  # IP 주소 (NULL 허용)
    logDate = Column(DateTime, nullable=False, default=datetime.datetime.now, server_default=text('CURRENT_TIMESTAMP')) 
    
class Chart(Base):
    __tablename__ = 'CHART'

    chartNum = Column(Integer, primary_key=True, autoincrement=True)  # 차트 고유번호
    labNum = Column(Integer, ForeignKey('LAB.labNum', ondelete='CASCADE'), nullable=True)  # 검사 정보
    historyNum = Column(Integer, ForeignKey('ITEMHISTORY.historyNum', ondelete='CASCADE'), nullable=True)  # 기록 정보
    reserveNum = Column(Integer, ForeignKey('RESERVE.reserveNum', ondelete='CASCADE'), nullable=True)  # 예약 정보
    chartNote = Column(String(1000))  # 진료 기록
    chartVital = Column(Integer)  # 환자 활력징후
    chartDate = Column(Date)  # 차트 작성일
    chartFile = Column(String(255))  # 첨부 파일 경로
    chartCode = Column(String(15))  # 차트 코드