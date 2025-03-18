import pydicom
import numpy as np
from PIL import Image
import io
import gdcm

def extract_dicom_info(filepath):
    # DICOM 파일 읽기
    dcm = pydicom.dcmread(filepath, force=True)

    dicom_info = {}
    image_files = []

    # 환자 정보
    patient_info_keys = [
        'PatientName', 'PatientID', 'PatientBirthDate', 'PatientSex',
        'PatientAge', 'PatientWeight'
    ]
    dicom_info['patient'] = {
        key: str(dcm.get(key, '정보 없음')) for key in patient_info_keys
    }

    # 연구 정보
    study_info_keys = [
        'StudyInstanceUID', 'StudyDate', 'StudyTime', 'StudyDescription',
        'Modality', 'ModalityInStudy', 'StudyID'
    ]
    dicom_info['study'] = {
        key: str(dcm.get(key, '정보 없음')) for key in study_info_keys
    }

    # 이미지 정보
    dicom_info['image'] = {
        'Rows': dcm.Rows if hasattr(dcm, 'Rows') else '정보 없음',
        'Columns': dcm.Columns if hasattr(dcm, 'Columns') else '정보 없음',
        'PixelSpacing': str(dcm.get('PixelSpacing', '정보 없음')),
        'SliceThickness': str(dcm.get('SliceThickness', '정보 없음')),
        'ImagePosition': str(dcm.get('ImagePosition', '정보 없음')),
        'ImageOrientation': str(dcm.get('ImageOrientation', '정보 없음'))
    }

    # 장비 정보
    device_info_keys = [
        'Manufacturer', 'ManufacturerModelName', 'DeviceSerialNumber',
        'SoftwareVersions', 'StationName'
    ]
    dicom_info['device'] = {
        key: str(dcm.get(key, '정보 없음')) for key in device_info_keys
    }

    # 의료 이미지 데이터
    pixel_info = {}
    if hasattr(dcm, 'pixel_array'):
        pixel_array = dcm.pixel_array
        pixel_info = {
            'shape': pixel_array.shape,
            'dtype': str(pixel_array.dtype),
            'min_value': pixel_array.min(),
            'max_value': pixel_array.max()
        }

    dicom_info['pixel_data'] = pixel_info

    # 추가 메타데이터 (일부 키)
    extra_keys = [
        'SOPInstanceUID', 'SOPClassUID', 'TransferSyntaxUID',
        'AccessionNumber', 'ReferringPhysicianName'
    ]
    dicom_info['additional_metadata'] = {
        key: str(dcm.get(key, '정보 없음')) for key in extra_keys
    }

    # JPEG 2000 형식 처리 (GDICOM을 이용)
    if hasattr(dcm, 'pixel_array'):
        pixel_array = dcm.pixel_array

        # JPEG 2000 인코딩 확인
        if hasattr(dcm, 'TransferSyntaxUID') and '1.2.840.10008.1.2.4.90' in dcm.TransferSyntaxUID:
            # GDICOM을 이용해 JPEG 2000 디코딩
            decoder = gdcm.ImageReader()
            decoder.SetFileName(filepath)
            if decoder.Read():
                pixel_array = decoder.GetImage().GetBuffer()

        # 16비트 이미지를 8비트로 변환하기 위한 정규화
        if pixel_array.dtype == np.uint16:
            # 최대값을 기준으로 정규화
            pixel_array = np.uint8(255 * (pixel_array - np.min(pixel_array)) / (np.max(pixel_array) - np.min(pixel_array)))

        # 3D 이미지 처리
        if len(pixel_array.shape) == 3:
            for i in range(pixel_array.shape[0]):
                img = Image.fromarray(pixel_array[i])
                img_byte_arr = io.BytesIO()
                img.save(img_byte_arr, format='PNG')
                img_byte_arr.seek(0)
                image_files.append(img_byte_arr)

        # 2D 이미지 처리
        elif len(pixel_array.shape) == 2:
            img = Image.fromarray(pixel_array)
            img_byte_arr = io.BytesIO()
            img.save(img_byte_arr, format='PNG')
            img_byte_arr.seek(0)
            image_files.append(img_byte_arr)

    return dicom_info, image_files
