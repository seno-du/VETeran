const express = require("express");
const multer = require("multer");
const axios = require("axios");
const cors = require("cors");
const FormData = require("form-data");
const fs = require("fs");

const app = express();
const upload = multer({ dest: "uploads/" });

app.use(cors());
app.use(express.json());

const COMPRE_FACE_API_KEY = "35ddfec8-e4d8-45c9-a129-d72737ecd0f9";
const COMPRE_FACE_URL = "http://localhost:8000/api/v1/verification/verify";

// 얼굴 검증 API 엔드포인트
app.post("/verify", upload.single("image"), async (req, res) => {
    try {
        // 업로드된 파일이 없을 경우 에러 반환
        if (!req.file) {
            return res.status(400).json({ error: "이미지를 업로드하세요!" });
        }

        // FormData 생성
        const formData = new FormData();
        formData.append("file", fs.createReadStream(req.file.path));
        formData.append("subject", "TestUser");

        // API 요청 보내기
        const response = await axios.post(COMPRE_FACE_URL, formData, {
            headers: {
                "x-api-key": COMPRE_FACE_API_KEY,
                ...formData.getHeaders(),
            },
        });

        res.json(response.data);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// 서버 실행
app.listen(5000, () => console.log("🚀 서버가 5000번 포트에서 실행 중!"));
