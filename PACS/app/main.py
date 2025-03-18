from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.utils import init_elasticsearch, insert_data
from app.routers import router
from fastapi import Depends
from sqlalchemy.orm import Session
from app.utils import get_db, engine
from app.utils import Base

app = FastAPI()

app.include_router(router)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
#서버에 배포할떄는 바꿔야 함

@app.get("/")
async def root():
    return {"message": "Hello World"}

@app.on_event("startup")
async def startup_event(
    db : Session = Depends(get_db)
):
    app.state.es = await init_elasticsearch()
    await insert_data()
    Base.metadata.create_all(bind=engine)
    print("Elasticsearch initialized")
