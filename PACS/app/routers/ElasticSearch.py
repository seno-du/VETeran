from fastapi import APIRouter, WebSocket
from app.utils import searchAPI, ConnectionManager
import json

router = APIRouter()
manager = ConnectionManager()

@router.get("/")
async def root():
    return {"message": "Hello ElasticSearch"}

# @router.get("/search")
# async def search(query):
#     res = searchAPI(query)
#     return res

@router.websocket("/ws/search")
async def websocket_endpoint(websocket: WebSocket):
    await manager.connect(websocket)
    try:
        while True:
            query = await websocket.receive_text()
            print(query)

            try:
                search_result = searchAPI(query)

                await manager.send_personal_message(
                    json.dumps({
                        "status": "success",
                        "data": search_result
                    }),
                    websocket
                )
            except Exception as e:
                # 에러 발생 시 에러 메시지 전송
                await manager.send_personal_message(
                    json.dumps({
                        "status": "error",
                        "message": str(e)
                    }),
                    websocket
                )

    except Exception as e:
        print(f"WebSocket error: {e}")
    finally:
        await manager.disconnect(websocket)