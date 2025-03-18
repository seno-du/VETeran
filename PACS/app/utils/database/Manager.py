from sqlalchemy import create_engine, Column, Integer, String, Date, Enum, ForeignKey, TIMESTAMP, text
from sqlalchemy.orm import declarative_base, sessionmaker, relationship

from .database import Base
import enum

class GenderEnum(enum.Enum):
    여성 = "여성"
    남성 = "남성"

class Manager(Base):
    __tablename__ = 'MANAGER'

    managerNum = Column(Integer, primary_key=True, autoincrement=True)  # 병원 관계자 고유번호 (PK)
    managerName = Column(String(100), nullable=False)  # 병원 관계자 이름
    managerLicenseNum = Column(String(5))  # 수의사 면허 번호
    managerId = Column(String(50), nullable=False, unique=True)  # 병원 관계자 로그인 ID
    managerPwd = Column(String(100), nullable=False)  # 병원 관계자 비밀번호 (암호화 필요)
    managerPhone = Column(String(15), nullable=False)  # 병원 관계자 연락처
    managerEmail = Column(String(100), nullable=False, unique=True)  # 병원 관계자 이메일
    managerBirth = Column(Date, nullable=False)  # 병원 관계자 생년월일
    managerGender = Column(Enum(GenderEnum), nullable=False)  # 병원 관계자 성별 (여성/남성)
    managerAddress = Column(String(50), nullable=False)  # 병원 관계자 주소
    managerSignupDate = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'))  # 가입 날짜
    managerImage = Column(String(255))  # 프로필 이미지 경로