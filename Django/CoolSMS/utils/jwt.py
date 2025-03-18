import base64
import datetime

import jwt
from dotenv import load_dotenv
import os

load_dotenv()

SECRET_KEY = os.getenv("JWT_SECRET")
print(SECRET_KEY)
SECRET_KEY_DECODED = base64.b64decode(SECRET_KEY)

def generate_jwt(uuid):
    payload = {
        "uuid" : uuid,
        "exp" : datetime.datetime.now() + datetime.timedelta(minutes=5)
    }
    return jwt.encode(payload, SECRET_KEY_DECODED, algorithm="HS256")
