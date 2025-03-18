import json
import sys

# 동물병원의 혈액검사 항목을 딕셔너리로 정의합니다.
blood_tests = {
    "CBC": {
        "RBC": {"min": "4.5", "max": "6.0", "unit": "M/μL"},
        "HGB": {"min": "12.0", "max": "16.0", "unit": "g/dL"},
        "HCT": {"min": "37", "max": "47", "unit": "%"},
        "WBC": {"min": "6.0", "max": "17.0", "unit": "x10^3/μL"},
        "MCV": {"min": "60", "max": "77", "unit": "fL"},
        "MCH": {"min": "20", "max": "25", "unit": "pg"},
        "MCHC": {"min": "32", "max": "36", "unit": "g/dL"},
        "RDW": {"min": "11", "max": "15", "unit": "%"},
        "%RETIC": {"min": "0.5", "max": "2.0", "unit": "%"}
    },
    "Biochemistry": {
        "Glucose": {"min": "70", "max": "110", "unit": "mg/dL"},
        "BUN": {"min": "10", "max": "30", "unit": "mg/dL"},
        "Creatinine": {"min": "0.6", "max": "1.4", "unit": "mg/dL"},
        "ALT": {"min": "10", "max": "50", "unit": "U/L"},
        "AST": {"min": "10", "max": "40", "unit": "U/L"},
        "Total Protein": {"min": "5.5", "max": "7.5", "unit": "g/dL"},
        "Albumin": {"min": "2.5", "max": "4.0", "unit": "g/dL"},
        "Globulin": {"min": "2.0", "max": "3.5", "unit": "g/dL"},
        "Bilirubin (Total)": {"min": "0.1", "max": "0.5", "unit": "mg/dL"}
    },
    "Coagulation": {
        "PT": {"min": "11", "max": "13.5", "unit": "seconds"},
        "aPTT": {"min": "25", "max": "35", "unit": "seconds"},
        "Fibrinogen": {"min": "150", "max": "400", "unit": "mg/dL"},
        "D-dimer": {"min": "0", "max": "0.5", "unit": "μg/mL"}
    },
    "Cardiac": {
        "NT-proBNP": {"min": "0", "max": "900", "unit": "pg/mL"},
        "Troponin I": {"min": "0", "max": "0.03", "unit": "ng/mL"}
    },
    "Renal (Kidney)": {
        "SDMA": {"min": "0", "max": "14", "unit": "μg/dL"},
        "Phosphorus": {"min": "2.5", "max": "5.5", "unit": "mg/dL"},
        "Calcium": {"min": "8.5", "max": "11.5", "unit": "mg/dL"}
    },
    "Inflammation": {
        "CRP": {"min": "0", "max": "1.0", "unit": "mg/dL"},
        "Haptoglobin": {"min": "0.3", "max": "3.0", "unit": "g/L"}
    },
    "Blood Gas": {
        "pH": {"min": "7.35", "max": "7.45", "unit": ""},
        "pCO2": {"min": "35", "max": "45", "unit": "mmHg"},
        "pO2": {"min": "80", "max": "100", "unit": "mmHg"},
        "HCO3": {"min": "22", "max": "26", "unit": "mEq/L"}
    },
    "Pancreatic": {
        "Amylase": {"min": "500", "max": "1500", "unit": "U/L"},
        "Lipase": {"min": "200", "max": "1800", "unit": "U/L"}
    }
}

def get_all_tests():
    return json.dumps({"tests": list(blood_tests.keys())})

def get_test_items(test_name):
    return json.dumps({"items": list(blood_tests.get(test_name, {}).keys())})

def get_test_details(test_name, item_name):
    return json.dumps({"details": blood_tests.get(test_name, {}).get(item_name, {})})

if __name__ == "__main__":
    if len(sys.argv) == 1:
        print(get_all_tests())  # 전체 혈액 검사 목록 반환
    elif len(sys.argv) == 2:
        print(get_test_items(sys.argv[1]))  # 특정 검사 항목 반환
    elif len(sys.argv) == 3:
        print(get_test_details(sys.argv[1], sys.argv[2]))  # 특정 검사 항목 기준값 반환
