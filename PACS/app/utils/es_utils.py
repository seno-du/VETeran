# app/utils/es_utils.py
from elasticsearch import Elasticsearch
from elasticsearch.exceptions import RequestError
import json
import pandas as pd
import os
from pathlib import Path

url = "http://localhost:9200"

def get_project_root():
    """프로젝트 루트 경로 반환"""
    current_file = Path(__file__)  # utils/es_utils.py
    return current_file.parent.parent  # app 디렉토리

async def init_elasticsearch():
    es = Elasticsearch([url])

    # 인덱스 존재 여부 확인
    index_name = 'animal_symptoms'
    if not es.indices.exists(index=index_name):
        # 매핑 파일 경로
        mapping_path = get_project_root() / 'asset' / 'mapping.json'

        with open(mapping_path, 'r', encoding='utf-8') as f:
            mapping = json.load(f)

        try:
            es.indices.create(index=index_name, body=mapping)
            print(f"Created index {index_name} with mappings")
        except RequestError as e:
            print(f"Error creating index: {e}")

    return es

async def insert_data():
    es = Elasticsearch([url])
    index_name = 'animal_symptoms'

    # 이미 인덱스가 있다면 삭제
    if es.indices.exists(index=index_name):
        es.indices.delete(index=index_name)

    # 매핑 파일 경로
    mapping_path = get_project_root() / 'asset' / 'mapping.json'

    with open(mapping_path, 'r', encoding='utf-8') as f:
        mapping = json.load(f)

    # 인덱스 생성
    es.indices.create(index=index_name, body=mapping)

    csv_path = get_project_root() / 'asset' / 'symtoms.csv'
    df = pd.read_csv(csv_path, encoding='euc-kr')

    # 데이터 색인
    for _, row in df.iterrows():
        data = {
            "증상코드": row['증상코드'],
            "증상분류_한글": row['증상분류 한글'],  # CSV 컬럼명에 맞게 수정
            "증상분류_영어": row['증상분류 영어'],  # CSV 컬럼명에 맞게 수정
            "증상목록코드": row['증상목록코드'],
            "증상명": row['증상명']
        }
        es.index(index=index_name, document=data)

    print(f"Indexed {len(df)} documents")

def searchAPI(query):
    es = Elasticsearch([url])

    search_query = {
        "query": {
            "bool": {
                "should": [
                    {
                        "multi_match": {
                            "query": query,
                            "fields": ["증상명^3", "증상분류_한글"],
                            "type": "most_fields"
                        }
                    },
                    {
                        "term": {
                            "증상명.keyword": {
                                "value": query,
                                "boost": 4
                            }
                        }
                    },
                    {
                        "match_phrase_prefix": {
                            "증상명": {
                                "query": query,
                                "boost": 2
                            }
                        }
                    }
                ],
                "minimum_should_match": 1
            }
        },
        "highlight": {
            "fields": {
                "증상명": {"type": "unified"},
                "증상분류_한글": {"type": "unified"}
            },
            "pre_tags": ["<em>"],
            "post_tags": ["</em>"]
        }
    }

    try:
        res = es.search(
            index="animal_symptoms",
            body=search_query,
            size=20
        )

        return {
            "total": res['hits']['total']['value'],
            "hits": [
                {
                    "score": hit["_score"],
                    "source": hit["_source"],
                    "highlight": hit.get("highlight", {})
                } for hit in res['hits']['hits']
            ]
        }

    except Exception as e:
        print(f"Search error: {str(e)}")
        raise e
