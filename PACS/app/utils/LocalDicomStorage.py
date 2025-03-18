import os
from datetime import datetime
import pydicom
from typing import Dict, Any
from io import BytesIO


class LocalDicomStorage:
    def __init__(self, storage_path: str):
        exists = os.path.exists(storage_path)
        if not exists:
            os.makedirs(storage_path)
        self.storage_path = storage_path

    def upload_dicom_file(self, file_content: bytes, patient_id: str) -> Dict[str, Any]:
        try:
            dcm = pydicom.dcmread(BytesIO(file_content))

            study_uid = dcm.StudyInstanceUID
            series_uid = dcm.SeriesInstanceUID
            instance_uid = dcm.SOPInstanceUID

            # /storage/patientID/studyUID/seriesUID/instanceUID.dcm
            file_path = os.path.join(
                self.storage_path,
                patient_id,
                study_uid,
                series_uid,
                f"{instance_uid}.dcm"
            )

            # 디렉토리 생성
            os.makedirs(os.path.dirname(file_path), exist_ok=True)

            # 파일 저장
            with open(file_path, 'wb') as buffer:
                buffer.write(file_content)

            return {
                'status': 'success',
                'patient_id': patient_id,
                'study_uid': study_uid,
                'series_uid': series_uid,
                'instance_uid': instance_uid,
                'local_path': file_path
            }

        except Exception as e:
            raise ValueError(f"Invalid DICOM file: {str(e)}")

    def get_dicom_file(self, instance_uid: str) -> Dict[str, Any]:
        # 파일 찾기 (디렉토리 탐색)
        for root, _, files in os.walk(os.path.join(self.storage_path, 'dicom')):
            for file in files:
                if file.startswith(f"{instance_uid}"):
                    file_path = os.path.join(root, file)

                    with open(file_path, 'rb') as f:
                        file_content = f.read()

                    # DICOM 파일 파싱해서 메타데이터 반환
                    dcm = pydicom.dcmread(BytesIO(file_content))

                    return {
                        'status': 'success',
                        'instance_uid': instance_uid,
                        'study_uid': dcm.StudyInstanceUID,
                        'series_uid': dcm.SeriesInstanceUID,
                        'file_contents': file_content,
                        'local_path': file_path
                    }

        raise FileNotFoundError(f"DICOM file not found: {instance_uid}")

    def get_study_files(self, study_uid: str) -> list:
        study_path = None

        # Study 경로 찾기
        for root, dirs, _ in os.walk(os.path.join(self.storage_path, 'dicom')):
            for dir in dirs:
                if dir == study_uid:
                    study_path = os.path.join(root, dir)
                    break
            if study_path:
                break

        if not study_path:
            raise FileNotFoundError(f"Study not found: {study_uid}")

        # Study 내 모든 DICOM 파일 수집
        dicom_files = []
        for root, _, files in os.walk(study_path):
            for file in files:
                if file.endswith('.dcm'):
                    # 파일 경로를 기준으로 DICOM 파일 읽기
                    instance_uid = file.replace('.dcm', '')  # .dcm 확장자를 제거하여 instance_uid
                    dicom_file = self.get_dicom_file(instance_uid)
                    dicom_files.append(dicom_file)  # 여러 DICOM 파일을 리스트에 추가

        return dicom_files
    def get_series_files(self, study_uid: str, series_uid: str) -> list:
        study_path = None

        # Study 경로 찾기
        for root, dirs, _ in os.walk(os.path.join(self.storage_path, 'dicom')):
            for dir in dirs:
                if dir == study_uid:
                    study_path = os.path.join(root, dir)
                    break
            if study_path:
                break

        if not study_path:
            raise FileNotFoundError(f"Study not found: {study_uid}")

        # 해당 series_uid에 속한 모든 DICOM 파일 수집
        dicom_files = []
        for root, _, files in os.walk(study_path):
            for file in files:
                if file.endswith('.dcm'):
                    instance_uid = file.replace('.dcm', '')  # .dcm 확장자를 제거하여 instance_uid
                    dicom_file = self.get_dicom_file(instance_uid)

                    # series_uid가 맞는지 확인
                    if dicom_file['series_uid'] == series_uid:
                        dicom_files.append(dicom_file)  # 해당 series_uid에 속하는 DICOM 파일만 추가

        return dicom_files