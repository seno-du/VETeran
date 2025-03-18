from pydantic_settings import BaseSettings
class Settings(BaseSettings):
    dicom_storage_dir: str = "./app/asset/dicom"
    metadata_storage_dir: str = "./app/asset/dicom_metadata"
    class Config:
        env_file = ".env"
    