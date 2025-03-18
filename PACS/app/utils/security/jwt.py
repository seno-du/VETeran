import jwt
import os
from dotenv import load_dotenv
import base64

key = base64.b64decode(os.getenv("JWT_SECRET"))

load_dotenv()

def extract_token(token : str) :
    try:
        decoded_payload = jwt.decode(token, key, algorithms=["HS256"])
        return decoded_payload
    except jwt.ExpiredSignatureError:
        print("토큰이 만료되었습니다.")
    except jwt.InvalidTokenError:
        print("유효하지 않은 토큰입니다.")