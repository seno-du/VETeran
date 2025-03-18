import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker,declarative_base, Session

load_dotenv()

DATABASE_URL = f'mysql+mysqlconnector://{os.getenv("DATABASE_USER")}:{os.getenv("DATABASE_PASSWORD")}@{os.getenv("DATABASE_HOST")}:{os.getenv("DATABASE_PORT")}/{os.getenv("DATABASE_NAME")}'

Base = declarative_base()

engine = create_engine(DATABASE_URL, pool_pre_ping=True)
session = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db: Session = session()
    try:
        yield db 
    finally:
        db.close()  