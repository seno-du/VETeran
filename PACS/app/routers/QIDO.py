from fastapi import APIRouter, Depends, Query, Request
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from typing import Optional, List, Dict
from datetime import datetime
from app.utils import get_db, MedicalFile, aspect
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Time, Date

# 라우터 설정
router = APIRouter()

# DICOM 태그와 컬럼 매핑
study_tags = {
    "PatientID": "petNum",
    "StudyDate": "studyDate",
    "StudyInstanceUID": "studyUid",
    "AccessionNumber": "accessionNumber",
    "StudyID": "studyId",
    "ModalitiesInStudy": "modalitiesInStudy"
}

series_tags = {
    "SeriesInstanceUID": "seriesUid",
    "SeriesNumber": "seriesNumber",
    "SeriesDescription": "seriesDescription",
    "Modality": "seriesModality"
}

instance_tags = {
    "SOPInstanceUID": "instanceUid",
    "InstanceNumber": "instanceNumber",
    "FrameOfReferenceUID": "frameOfReferenceUID"
}

# 날짜/시간 형식 변환 함수
def format_date(date):
    return date.strftime("%Y%m%d") if date else None

def format_time(time):
    return time.strftime("%H%M%S") if time else None

# 쿼리 필터링 함수
def filter_query(query, params: Dict, tag_mapping: Dict, model):
    for key, value in params.items():
        if key in tag_mapping and value:
            column = getattr(model, tag_mapping[key])
            query = query.filter(column == value)
    return query

# 페이지네이션 함수
def apply_pagination(query, limit: Optional[int], offset: Optional[int]):
    if offset:
        query = query.offset(offset)
    if limit:
        query = query.limit(limit)
    return query

# 응답 형식화 함수
def format_response(results, includefield: Optional[List[str]], level: str):
    if not results:
        return []
    response = []
    for result in results:
        if level == "study":
            item = {
                "StudyInstanceUID": result.studyUid,
                "PatientID": str(result.petNum),  # PatientID는 문자열로
                "StudyDate": format_date(result.studyDate),
                "StudyTime": format_time(result.studyTime),
                "AccessionNumber": result.accessionNumber,
                "StudyID": result.studyId,
                "ModalitiesInStudy": result.modalitiesInStudy
            }
        elif level == "series":
            item = {
                "SeriesInstanceUID": result.seriesUid,
                "SeriesNumber": str(result.seriesNumber) if result.seriesNumber else None,
                "SeriesDescription": result.seriesDescription,
                "Modality": result.seriesModality
            }
        elif level == "instance":
            item = {
                "SOPInstanceUID": result.instanceUid,
                "InstanceNumber": str(result.instanceNumber),
                "FrameOfReferenceUID": result.frameOfReferenceUID
            }
        if includefield and "all" not in includefield:
            item = {k: v for k, v in item.items() if k in includefield}
        response.append(item)
    return response

# Studies 검색 엔드포인트
@router.get("/studies")
@aspect.apply
async def get_studies(
    request: Request,
    limit: Optional[int] = Query(None),
    offset: Optional[int] = Query(None),
    includefield: Optional[List[str]] = Query(None),
    db: Session = Depends(get_db)
):
    query = db.query(MedicalFile).distinct(MedicalFile.studyUid)
    params = request.query_params
    query = filter_query(query, params, study_tags, MedicalFile)
    query = apply_pagination(query, limit, offset)
    results = query.all()
    response = format_response(results, includefield, "study")
    return JSONResponse(content=response)

# Series 검색 엔드포인트
@router.get("/studies/{studyUID}/series")
async def get_series(
    studyUID: str,
    request: Request,
    limit: Optional[int] = Query(None),
    offset: Optional[int] = Query(None),
    includefield: Optional[List[str]] = Query(None),
    db: Session = Depends(get_db)
):
    query = db.query(MedicalFile).filter(MedicalFile.studyUid == studyUID).distinct(MedicalFile.seriesUid)
    params = request.query_params
    query = filter_query(query, params, series_tags, MedicalFile)
    query = apply_pagination(query, limit, offset)
    results = query.all()
    response = format_response(results, includefield, "series")
    return JSONResponse(content=response)

# Instances 검색 엔드포인트
@router.get("/studies/{studyUID}/series/{seriesUID}/instances")
async def get_instances(
    studyUID: str,
    seriesUID: str,
    request: Request,
    limit: Optional[int] = Query(None),
    offset: Optional[int] = Query(None),
    includefield: Optional[List[str]] = Query(None),
    db: Session = Depends(get_db)
):
    query = db.query(MedicalFile).filter(
        MedicalFile.studyUid == studyUID,
        MedicalFile.seriesUid == seriesUID
    )
    params = request.query_params
    query = filter_query(query, params, instance_tags, MedicalFile)
    query = apply_pagination(query, limit, offset)
    results = query.all()
    response = format_response(results, includefield, "instance")
    return JSONResponse(content=response)