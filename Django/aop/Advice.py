from aophelper.baseAdvice import BaseAdvice
from database import Log, get_db
from sqlalchemy.orm import Session
import json
from django.http import HttpRequest, JsonResponse

class Advice(BaseAdvice):
    async def before(self, func, *args, **kwargs):
        print("before 실행")
        request = kwargs.get("request") or kwargs.get("websocket")
        if request is None and args:
            request = args[0]
            if type(request) is HttpRequest:
                raise RuntimeError("Request가 매개변수에 존재하지 않거나 첫번 째 인자로 존재하지 않습니다")
        
        # JWT 로직이 처리될 곳
        
    def after(self, func, result, *args, **kwargs):
        print("after 호출")
        request = kwargs.get("request") or kwargs.get("websocket")
        if request is None and args:
            request = args[0]
        
        db = kwargs.get("db")
        if db is None:
            db = next(get_db())
            
        EXCLUDED_HEADERS = {"authorization", "cookie", "set-cookie"}
        
        if isinstance(result, JsonResponse):
            result_data = result.content.decode('utf-8')  # JsonResponse의 content에서 실제 데이터를 추출
        else:
            result_data = json.dumps(result, default=str)
            
        log_data = {
            "method" : request.method if request and hasattr(request, "method") else None,
            "header": {
                k: v for k, v in request.headers.items() if k.lower() not in EXCLUDED_HEADERS
            },
            "result": result_data
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
        if request is None and args:
            request = args[0]
        
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
        db.commit()