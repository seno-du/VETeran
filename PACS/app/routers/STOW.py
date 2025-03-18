import os
from io import BytesIO
from email.policy import default
from email import message_from_bytes
import pydicom
from pydicom import dcmread
from pydicom.valuerep import PersonName
from fastapi import APIRouter, Request, HTTPException, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from app.utils import get_db, MedicalFile  # MedicalFile 모델 import
import datetime

BULK_DATA_BASE_URL = "http://localhost:8000/dicom/"  # Bulk Data 기본 URL (슬래시 추가)
STORAGE_DIR = "./app/asset/dicom"  # DICOM 파일 저장 디렉토리
os.makedirs(STORAGE_DIR, exist_ok=True)  # 디렉토리 존재하지 않으면 생성

router = APIRouter()

# ------------------------------------------------------------------
# STOW‑RS: DICOM 파일 업로드 (POST /studies)
# ------------------------------------------------------------------

@router.post("/studies", tags=["STOW-RS"])
async def stow_rs_upload(request: Request, db: Session = Depends(get_db)):
    """
    DICOM 파일을 STOW-RS 표준에 따라 업로드하고 저장합니다.
    multipart/related Content-Type 요청만 처리합니다.
    """
    content_type = request.headers.get("Content-Type", "")
    if not content_type.startswith("multipart/related"):
        raise HTTPException(status_code=400, detail="Invalid Content-Type. Expected 'multipart/related'")

    body = await request.body()
    try:
        msg = message_from_bytes(body, policy=default)
        parts = list(msg.iter_parts())
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to parse multipart message: {e}")

    stored_instances = []
    failed_instances = []

    for part in parts:
        part_content_type = part.get_content_type()
        try:
            if part_content_type == "application/dicom":
                payload = part.get_payload(decode=True)
                dataset = dcmread(BytesIO(payload))
            elif part_content_type == "application/dicom+json":
                payload = part.get_payload(decode=True)
                json_str = payload.decode("utf-8")
                dataset = pydicom.Dataset.from_json(json_str)
            else:
                failed_instances.append({"error": f"Unsupported Content-Type: {part_content_type}"})
                continue

            # 메타데이터 추출
            study_uid = str(getattr(dataset, "StudyInstanceUID", "Unknown"))
            series_uid = str(getattr(dataset, "SeriesInstanceUID", "Unknown"))
            sop_uid = str(getattr(dataset, "SOPInstanceUID", "Unknown"))
            sop_class_uid = str(getattr(dataset, "SOPClassUID", "Unknown"))  # SOP Class UID 추가
            pet_num = str(getattr(dataset, "PatientID", None))
            study_time_str = getattr(dataset, "StudyTime", None)
            accession_number = getattr(dataset, "AccessionNumber", None)
            study_id = getattr(dataset, "StudyID", None)
            modalities = getattr(dataset, "ModalitiesInStudy", None)
            series_modality = getattr(dataset, "Modality", None)
            series_number = getattr(dataset, "SeriesNumber", None)
            series_description = getattr(dataset, "SeriesDescription", None)
            modality = str(getattr(dataset, "Modality", None))
            study_date_str = getattr(dataset, "StudyDate", None)
            manufacturer = getattr(dataset, "Manufacturer", None)
            manufacturer_model_name = getattr(dataset, "ManufacturerModelName", None)
            instance_number = getattr(dataset, "InstanceNumber", None)
            frame_of_reference_uid = getattr(dataset, "FrameOfReferenceUID", None)

            # StudyTime 파싱
            study_time = None
            if study_time_str:
                try:
                    study_time = datetime.datetime.strptime(study_time_str, "%H%M%S").time()
                except ValueError:
                    study_time = None

            # ModalitiesInStudy 변환
            modalities_str = None
            if modalities:
                if isinstance(modalities, (list, pydicom.multival.MultiValue)):
                    modalities_str = ",".join(modalities)
                else:
                    modalities_str = str(modalities)

            # StudyDate 파싱
            study_date = None
            if study_date_str:
                try:
                    study_date = datetime.datetime.strptime(study_date_str, "%Y%m%d").date()
                except ValueError:
                    study_date = None

            # 파일 저장
            study_path = os.path.join(STORAGE_DIR, study_uid)
            series_path = os.path.join(study_path, series_uid)
            os.makedirs(series_path, exist_ok=True)
            file_path = os.path.join(series_path, f"{sop_uid}.dcm")
            dataset.save_as(file_path, enforce_file_format=True)
            file_size = os.path.getsize(file_path)

            # 표준 Retrieve URL 생성
            bulk_data_uri = f"{BULK_DATA_BASE_URL}{study_uid}/series/{series_uid}/instances/{sop_uid}"

            # DB 저장
            record = MedicalFile(
                managerNum=22,  # 임시 값, 실제 환경에 맞게 수정 필요
                petNum=pet_num,
                medicalDate=datetime.datetime.now(),
                medicalNote=None,
                studyUid=study_uid,
                seriesUid=series_uid,
                instanceUid=sop_uid,
                studyTime=study_time,
                accessionNumber=accession_number,
                studyId=study_id,
                modalitiesInStudy=modalities_str,
                seriesModality=series_modality,
                seriesNumber=series_number,
                seriesDescription=series_description,
                modality=modality,
                studyDate=study_date,
                manufacturer=manufacturer,
                manufacturerModelName=manufacturer_model_name,
                instanceNumber=instance_number,
                frameOfReferenceUID=frame_of_reference_uid,
                filePath=file_path,
                fileSize=file_size,
            )

            db.add(record)
            db.commit()

            stored_instances.append({
                "id": record.medicalNum,
                "StudyInstanceUID": study_uid,
                "SeriesInstanceUID": series_uid,
                "SOPInstanceUID": sop_uid,
                "SOPClassUID": sop_class_uid,  # SOP Class UID 추가
                "PatientID": pet_num,
                "FilePath": file_path,
                "RetrieveURL": bulk_data_uri
            })

        except Exception as e:
            db.rollback()
            print(e)
            failed_instances.append({
                "error": str(e),
                "ContentType": part_content_type
            })

    # 응답 생성
    if stored_instances and not failed_instances:
        status_code = 200
        response_data = {
            "0008,1190": {  # Retrieve URL (Study 수준)
                "vr": "UR",
                "Value": [f"{request.url.scheme}://{request.client.host}:{request.url.port}/studies"]
            },
            "0008,1199": {  # Referenced SOP Sequence
                "vr": "SQ",
                "Value": [
                    {
                        "0008,1150": {"vr": "UI", "Value": [inst["SOPClassUID"]]},  # SOP Class UID 사용
                        "0008,1155": {"vr": "UI", "Value": [inst["SOPInstanceUID"]]},
                        "0008,1190": {"vr": "UR", "Value": [inst["RetrieveURL"]]}
                    } for inst in stored_instances
                ]
            }
        }
    elif stored_instances and failed_instances:
        status_code = 202
        response_data = {
            "0008,1190": {
                "vr": "UR",
                "Value": [f"{request.url.scheme}://{request.client.host}:{request.url.port}/studies"]
            },
            "0008,1198": {  # Failed SOP Sequence
                "vr": "SQ",
                "Value": [
                    {
                        "0008,1150": {"vr": "UI", "Value": ["Unknown"]},
                        "0008,1155": {"vr": "UI", "Value": ["Unknown"]},
                        "0008,1197": {"vr": "US", "Value": [409]},
                        "0009,1097": {"vr": "LO", "Value": [fail.get("error", "Error")]}
                    } for fail in failed_instances
                ]
            },
            "0008,1199": {  # Referenced SOP Sequence
                "vr": "SQ",
                "Value": [
                    {
                        "0008,1150": {"vr": "UI", "Value": [inst["SOPClassUID"]]},  # SOP Class UID 사용
                        "0008,1155": {"vr": "UI", "Value": [inst["SOPInstanceUID"]]},
                        "0008,1190": {"vr": "UR", "Value": [inst["RetrieveURL"]]}
                    } for inst in stored_instances
                ]
            }
        }
    else:
        status_code = 409
        response_data = {
            "0008,1190": {
                "vr": "UR",
                "Value": [f"{request.url.scheme}://{request.client.host}:{request.url.port}/studies"]
            },
            "0008,1198": {  # Failed SOP Sequence
                "vr": "SQ",
                "Value": [
                    {
                        "0008,1150": {"vr": "UI", "Value": ["Unknown"]},
                        "0008,1155": {"vr": "UI", "Value": ["Unknown"]},
                        "0008,1197": {"vr": "US", "Value": [409]},
                        "0009,1097": {"vr": "LO", "Value": ["No instance stored"]}
                    }
                ]
            }
        }

    return JSONResponse(status_code=status_code, content=response_data)