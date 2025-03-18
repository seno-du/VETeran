from fastapi import APIRouter
from .ElasticSearch import router as elsatic_router
from .STOW import router as stow_router
from .QIDO import router as qido_router
from .WADO import router as wado_router

router = APIRouter()

# 각 모듈의 라우터 포함
router.include_router(elsatic_router, prefix="/elasticsearch", tags=["ElasticSearch"])
router.include_router(stow_router, prefix="/dicom-web", tags=["dicom-web"])
router.include_router(qido_router, prefix="/dicom-web", tags=["dicom-web"])
router.include_router(wado_router, prefix="/dicom-web", tags=["dicom-web"])
