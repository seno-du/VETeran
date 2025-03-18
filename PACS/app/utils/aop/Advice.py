from aophelper.baseAdvice import BaseAdvice
from app.utils import Log, get_db, extract_token
from sqlalchemy.orm import Session
from fastapi.encoders import jsonable_encoder
import json

class Advice(BaseAdvice):
    def before(self, func, *args, **kwargs):
        print("before 실행")
        request = kwargs.get("request") or kwargs.get("websocket")
        if request is None:
            raise RuntimeError("Request가 매개변수에 존재하지 않습니다")
        
        token = request.headers.get("Authorization")[7:]
        
        if token is None:
            raise RuntimeError("Authorization이 header에 존재하지 않습니다")
        
        extracted_token = extract_token(token)
        
        if extracted_token.get("type") is None or extracted_token.get("type") != "manager":
            raise RuntimeError("잘못된 접근입니다")
        
    def after(self, func, result, *args, **kwargs):
        print("after 호출")
        request = kwargs.get("request") or kwargs.get("websocket")
        db = kwargs.get("db")
        if db is None:
            db = next(get_db())
            
        EXCLUDED_HEADERS = {}

            
        log_data = {
            "method" : request.method if request and hasattr(request, "method") else None,
            "header": {
                k: v for k, v in request.headers.items() if k.lower() not in EXCLUDED_HEADERS
            },
            "result": jsonable_encoder(result)
        }
        
        info_log = Log()
        info_log.logRemoteAddr =  request.client.host if request and hasattr(request, "client") and hasattr(request.client, "host") else "UNKNOWN"
        info_log.logCategory = "INFO"
        info_log.logDetail = json.dumps(log_data, ensure_ascii=False)
        info_log.logLocation = func.__name__
        db.add(info_log)
        db.commit()
                    
    def around(self, func, *args, **kwargs):
        result = func(*args, **kwargs)
        return result

    def on_exception(self, func, exception, *args, **kwargs):
        request = kwargs.get("request") or kwargs.get("websocket")
        db = kwargs.get("db")
        if db is None:
            db = next(get_db())
        print(exception)
        error_log = Log()
        error_log.logRemoteAddr =  request.client.host if request and hasattr(request, "client") and hasattr(request.client, "host") else "UNKNOWN"
        error_log.logCategory = "ERROR"
        error_log.logDetail = f'{{"args": "{args}", "kwargs": "{kwargs}", "exception": "{str(exception)}"}}'
        error_log.logLocation = func.__name__
        db.add(error_log)
        db.commit();