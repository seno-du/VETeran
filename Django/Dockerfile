# Miniconda 이미지를 사용하여 Conda 설치
FROM continuumio/miniconda3

# 작업 디렉토리 설정
WORKDIR /app

# Conda 환경 파일을 복사
COPY environment.yml /app/

# Conda 환경 생성 (Python 3.10.10 버전으로)
RUN conda env create -f environment.yml

# 환경을 활성화하고 실행할 명령어 설정
SHELL ["conda", "run", "-n", "myenv", "/bin/bash", "-c"]

# Django 프로젝트 파일들을 컨테이너로 복사
COPY . /app/

# Django 프로젝트의 manage.py 파일을 실행
CMD ["conda", "run", "-n", "myenv", "python", "manage.py", "runserver"]
