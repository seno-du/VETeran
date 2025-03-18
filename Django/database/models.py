from sqlalchemy import Column, Integer, String, Text, Date, Enum, TIMESTAMP, ForeignKey
from sqlalchemy.dialects.mysql import ENUM
from .database import Base
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Time, Enum, DECIMAL, Date, text
from sqlalchemy.dialects.mysql import LONGTEXT
import datetime

# USER 모델 정의
class USER(Base):
    __tablename__ = 'USER'

    userNum = Column(Integer, primary_key=True, autoincrement=True)  # 사용자 고유번호 (PK)
    userName = Column(String(50), nullable=False)  # 사용자 이름
    userId = Column(String(50), nullable=False, unique=True)  # 사용자 로그인 ID (유니크)
    userPwd = Column(String(100), nullable=False)  # 사용자 비밀번호
    userPhone = Column(String(15), nullable=False, unique=True)  # 사용자 연락처
    userBirth = Column(Date, nullable=False)  # 사용자 생년월일
    userEmail = Column(String(100), nullable=False, unique=True)  # 사용자 이메일 (고유값)
    userAddress = Column(String(100), nullable=False)  # 사용자 주소
    userAddressNum = Column(String(5), nullable=False)  # 사용자 주소 우편번호
    userStatus = Column(ENUM('활성', '비활성', name='user_status_enum'), default='활성', nullable=False)  # 사용자 상태
    userSignupDate = Column(TIMESTAMP, default='CURRENT_TIMESTAMP')  # 사용자 가입 일시

# RESERVE 모델 정의 (이전 코드와 동일)
class RESERVE(Base):
    __tablename__ = 'RESERVE'

    reserveNum = Column(Integer, primary_key=True, autoincrement=True)  # 예약 고유번호 (PK)
    petNum = Column(Integer, ForeignKey('PET.petNum', ondelete='CASCADE'))  # 반려동물 고유번호 (FK)
    managerNum = Column(Integer, ForeignKey('MANAGER.managerNum', ondelete='CASCADE'))  # 병원 관계자(수의사) 고유번호 (FK)
    reserveStatus = Column(ENUM('대기', '완료', '취소', name='reserve_status_enum'), default='대기', nullable=False)  # 예약 상태
    reserveDate = Column(TIMESTAMP, nullable=False)  # 예약 날짜
    reserveNotice = Column(Text, nullable=False)  # 예약 관련 메모

class MedicalFile(Base):
    __tablename__ = 'MEDICALFILE'
    medicalNum = Column(Integer, primary_key=True, autoincrement=True) # 고유번호: 각 의료 파일 항목을 식별하는 고유한 숫자입니다. 테이블의 기본 키(Primary Key)이며, 자동으로 증가(autoincrement)합니다.
    managerNum = Column(Integer, ForeignKey('MANAGER.managerNum', ondelete='CASCADE'), nullable=False) # 등록자 번호: 의료 파일을 등록한 관리자(수의사, 테크니션 등)의 고유 번호입니다. 'MANAGER' 테이블의 'managerNum'을 참조하는 외래 키(Foreign Key)이며, 'CASCADE' 옵션으로 연결된 관리자 정보가 삭제되면 해당 의료 파일도 함께 삭제됩니다. 필수 입력 값(nullable=False)입니다.
    petNum = Column(Integer, ForeignKey("PET.petNum", ondelete="CASCADE"), nullable=False) # 환자(반려동물) 번호: 의료 파일과 관련된 환자(반려동물)의 고유 번호입니다. 'PET' 테이블의 'petNum'을 참조하는 외래 키이며, 'CASCADE' 옵션으로 연결된 반려동물 정보가 삭제되면 해당 의료 파일도 함께 삭제됩니다. 필수 입력 값입니다.
    medicalDate = Column(DateTime, nullable=False) # 입력한 날짜, 시간: 의료 파일이 시스템에 입력된 날짜와 시간입니다. 필수 입력 값입니다.
    medicalNote = Column(String(1000)) # 해당 영상물에 대한 노트: 의료 영상물에 대한 추가 설명 또는 메모를 저장하는 문자열입니다. 최대 1000자까지 입력 가능합니다.
    studyUid = Column(String(100)) # study_uid: DICOM 연구(Study)를 식별하는 고유한 UID(Unique Identifier)입니다. 최대 100자까지 입력 가능합니다.
    seriesUid = Column(String(100)) # series_uid: DICOM 시리즈(Series)를 식별하는 고유한 UID입니다. 최대 100자까지 입력 가능합니다.
    instanceUid = Column(String(100)) # instance_uid: DICOM 인스턴스(Instance)를 식별하는 고유한 UID입니다. 최대 100자까지 입력 가능합니다.
    studyTime = Column(Time) # 촬영 날짜/시간: DICOM 영상 촬영 날짜와 시간입니다. Time 형식으로 저장됩니다.
    accessionNumber = Column(String(50)) # 접수 번호: 의료 영상물 접수 번호입니다. 최대 50자까지 입력 가능합니다.
    studyId = Column(String(50)) # 검사 ID: DICOM 연구(Study) ID입니다. 최대 50자까지 입력 가능합니다.
    patientName = Column(String(100)) # 환자 이름: DICOM 파일에 포함된 환자 이름입니다. 최대 100자까지 입력 가능합니다.
    modalitiesInStudy = Column(String(50)) # 검사에 사용된 장비 종류: 해당 연구(Study)에 사용된 의료 장비 종류(예: CT, MRI, X-ray)입니다. 최대 50자까지 입력 가능합니다.
    seriesModality = Column(String(50)) # 시리즈 장비 종류: 해당 시리즈(Series)에 사용된 의료 장비 종류입니다. 최대 50자까지 입력 가능합니다.
    seriesNumber = Column(Integer) # 시리즈 번호: 해당 시리즈(Series)의 순번입니다.
    seriesDescription = Column(String(200)) # 시리즈 설명: 해당 시리즈(Series)에 대한 설명입니다. 최대 200자까지 입력 가능합니다.
    modality = Column(String(50)) # 이미지 모달리티: 인스턴스(Instance)의 모달리티를 나타냅니다.
    # DICOM 파일 정보
    studyDate = Column(Date) # 검사일: DICOM 영상 촬영 날짜입니다. Date 형식으로 저장되며, 필수 입력 값입니다.
    manufacturer = Column(String(100)) # 장비 제조사: DICOM 영상 촬영에 사용된 장비 제조사입니다. 최대 100자까지 입력 가능합니다.
    manufacturerModelName = Column(String(100)) # 장비 모델명: DICOM 영상 촬영에 사용된 장비 모델명입니다. 최대 100자까지 입력 가능합니다.
    instanceNumber = Column(Integer, nullable=False) # 이미지 번호: DICOM 인스턴스(Instance)의 순번입니다. 필수 입력 값입니다.
    frameOfReferenceUID = Column(String(100)) # 프레임 오브 레퍼런스 UID: DICOM 프레임 오브 레퍼런스(Frame of Reference) UID입니다. 최대 100자까지 입력 가능합니다.
    # 저장 및 파일 정보
    filePath = Column(String(200), nullable=False) # 파일 경로: DICOM 파일이 저장된 실제 파일 시스템 경로입니다. 필수 입력 값입니다.
    fileSize = Column(Integer, nullable=False) # 파일 크기: DICOM 파일 크기입니다. 필수 입력 값입니다.
    
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
