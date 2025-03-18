import secrets
from typing import List
from io import BytesIO
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from pydicom import dcmread
from pydicom.errors import InvalidDicomError
from PIL import Image
import numpy as np
from app.utils import MedicalFile, get_db

router = APIRouter()
# 멀티파트/관련 응답 생성 헬퍼 함수
def create_multipart_response(files: List[MedicalFile]):
    boundary = secrets.token_hex(16)
    content_type = f"multipart/related; boundary={boundary}; type=application/dicom"

    def generate():
        # Manifest 생성 (인스턴스 UID 목록)
        manifest = "\n".join([f"{file.instanceUid}" for file in files])
        yield f"--{boundary}\r\n"
        yield "Content-Type: text/plain\r\n\r\n"
        yield f"{manifest}\r\n"

        # 각 DICOM 파일 추가
        for file in files:
            try:
                with open(file.filePath, "rb") as f:
                    dicom_data = f.read()
                yield f"--{boundary}\r\n"
                yield f"Content-Type: application/dicom\r\n"
                yield f"Content-ID: <urn:oid:{file.instanceUid}>\r\n\r\n"
                yield dicom_data
                yield "\r\n"
            except FileNotFoundError:
                continue  # 파일이 없으면 건너뜀
        yield f"--{boundary}--\r\n"

    return StreamingResponse(generate(), media_type=content_type)

# 1. 연구 전체 검색
@router.get("/studies/{study_uid}")
def retrieve_study(study_uid: str, db: Session = Depends(get_db)):
    files = db.query(MedicalFile).filter(MedicalFile.studyUid == study_uid).all()
    if not files:
        raise HTTPException(status_code=404, detail="Study not found")
    return create_multipart_response(files)

# 2. 시리즈 검색
@router.get("/studies/{study_uid}/series/{series_uid}")
def retrieve_series(study_uid: str, series_uid: str, db: Session = Depends(get_db)):
    files = db.query(MedicalFile).filter(
        MedicalFile.studyUid == study_uid,
        MedicalFile.seriesUid == series_uid
    ).all()
    if not files:
        raise HTTPException(status_code=404, detail="Series not found")
    return create_multipart_response(files)

# 3. 인스턴스 검색
@router.get("/studies/{study_uid}/series/{series_uid}/instances/{instance_uid}")
def retrieve_instance(study_uid: str, series_uid: str, instance_uid: str, db: Session = Depends(get_db)):
    file = db.query(MedicalFile).filter(
        MedicalFile.studyUid == study_uid,
        MedicalFile.seriesUid == series_uid,
        MedicalFile.instanceUid == instance_uid
    ).first()
    if not file or not file.filePath:
        raise HTTPException(status_code=404, detail="Instance not found")
    
    try:
        with open(file.filePath, "rb") as f:
            dicom_data = f.read()
        return StreamingResponse(
            BytesIO(dicom_data),
            media_type="application/dicom",
            headers={"Content-ID": f"<urn:oid:{instance_uid}>"}
        )
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="DICOM file not found")

# 4. 메타데이터 검색
@router.get("/studies/{study_uid}/series/{series_uid}/instances/{instance_uid}/metadata")
def retrieve_metadata(study_uid: str, series_uid: str, instance_uid: str, db: Session = Depends(get_db)):
    file = db.query(MedicalFile).filter(
        MedicalFile.studyUid == study_uid,
        MedicalFile.seriesUid == series_uid,
        MedicalFile.instanceUid == instance_uid
    ).first()
    if not file or not file.filePath:
        raise HTTPException(status_code=404, detail="Instance not found")
    
    try:
        dataset = dcmread(file.filePath)
        dataset.PixelData = None  # 픽셀 데이터 제거
        buffer = BytesIO()
        dataset.save_as(buffer, write_like_original=False)
        buffer.seek(0)
        return StreamingResponse(
            buffer,
            media_type="application/dicom",
            headers={"Content-ID": f"<urn:oid:{instance_uid}>"}
        )
    except InvalidDicomError:
        raise HTTPException(status_code=500, detail="Invalid DICOM file")

# 5. 프레임 추출
@router.get("/studies/{study_uid}/series/{series_uid}/instances/{instance_uid}/frames/{frame_numbers}")
def retrieve_frames(study_uid: str, series_uid: str, instance_uid: str, frame_numbers: str, db: Session = Depends(get_db)):
    file = db.query(MedicalFile).filter(
        MedicalFile.studyUid == study_uid,
        MedicalFile.seriesUid == series_uid,
        MedicalFile.instanceUid == instance_uid
    ).first()
    if not file or not file.filePath:
        raise HTTPException(status_code=404, detail="Instance not found")
    
    try:
        dataset = dcmread(file.filePath)
        if "NumberOfFrames" not in dataset or int(dataset.NumberOfFrames) < 1:
            raise HTTPException(status_code=400, detail="No frames available")
        
        frame_indices = [int(f) - 1 for f in frame_numbers.split(",")]  # 1-based to 0-based
        max_frame = int(dataset.NumberOfFrames) - 1
        if any(i < 0 or i > max_frame for i in frame_indices):
            raise HTTPException(status_code=400, detail="Invalid frame number")

        # 간단한 프레임 추출 (압축되지 않은 데이터 가정)
        pixel_data = dataset.pixel_array
        frame_data = pixel_data[frame_indices]
        dataset.PixelData = frame_data.tobytes()
        dataset.NumberOfFrames = len(frame_indices)
        
        buffer = BytesIO()
        dataset.save_as(buffer, write_like_original=False)
        buffer.seek(0)
        return StreamingResponse(
            buffer,
            media_type="application/dicom",
            headers={"Content-ID": f"<urn:oid:{instance_uid}>"}
        )
    except InvalidDicomError:
        raise HTTPException(status_code=500, detail="Invalid DICOM file")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Frame extraction failed: {str(e)}")

# 6. 렌더링된 이미지
@router.get("/studies/{study_uid}/series/{series_uid}/instances/{instance_uid}/rendered")
def retrieve_rendered(study_uid: str, series_uid: str, instance_uid: str, db: Session = Depends(get_db)):
    file = db.query(MedicalFile).filter(
        MedicalFile.studyUid == study_uid,
        MedicalFile.seriesUid == series_uid,
        MedicalFile.instanceUid == instance_uid
    ).first()
    if not file or not file.filePath:
        raise HTTPException(status_code=404, detail="Instance not found")
    
    try:
        dataset = dcmread(file.filePath)
        pixel_array = dataset.pixel_array
        if len(pixel_array.shape) > 2:  # 다중 프레임의 경우 첫 번째 프레임 사용
            pixel_array = pixel_array[0]
        
        # 픽셀 데이터를 0-255로 스케일링 (간단한 예시)
        pixel_array = ((pixel_array - pixel_array.min()) * 255 / (pixel_array.max() - pixel_array.min())).astype(np.uint8)
        img = Image.fromarray(pixel_array)
        buffer = BytesIO()
        img.convert("RGB").save(buffer, format="JPEG")
        buffer.seek(0)
        return StreamingResponse(buffer, media_type="image/jpeg")
    except InvalidDicomError:
        raise HTTPException(status_code=500, detail="Invalid DICOM file")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Rendering failed: {str(e)}")
