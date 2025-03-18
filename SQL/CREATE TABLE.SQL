-- 보호자 테이블
CREATE TABLE `USER` (
    userNum INT PRIMARY KEY AUTO_INCREMENT, 	-- 사용자 고유번호 (PK)
    userName VARCHAR(50) NOT NULL, 			-- 사용자 이름
    userId VARCHAR(50) NOT NULL UNIQUE, 		-- 사용자 로그인 ID (유니크)
    userPwd VARCHAR(100) NOT NULL, 			-- 사용자 비밀번호
    userPhone VARCHAR(15) NOT NULL UNIQUE,			-- 사용자 연락처
    userBirth DATE NOT NULL,					-- 사용자 생년월일
    userEmail VARCHAR(100) NOT NULL UNIQUE, 	-- 사용자 이메일 (고유값)
    userAddress VARCHAR(100) NOT NULL, 		-- 사용자 주소
    userAddressNum VARCHAR(5) NOT NULL, 			-- 사용자 주소 우편번호
    userStatus ENUM('활성','비활성') NOT NULL DEFAULT(1), -- 사용자 상태
    userSignupDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 환자(강아지, 고양이) 테이블
CREATE TABLE PET (
    petNum INT PRIMARY KEY AUTO_INCREMENT, -- 반려동물 고유번호 (PK)
    userNum INT NOT NULL, 					-- 보호자(사용자) 고유번호 (FK)
    petSpecies ENUM('강아지','고양이') NOT NULL,-- 동물 종 (강아지, 고양이)
    petColor VARCHAR(20) NOT NULL,			-- 동물 모색
    petName VARCHAR(15) NOT NULL, 			-- 반려동물 이름
    petBreed VARCHAR(20) NOT NULL, 		-- 반려동물 품종
    petGender ENUM('암컷','수컷','중성화암컷','중성화수컷') NOT NULL, -- 반려동물 성별
    petBirth DATE NOT NULL, 				-- 반려동물 생년월일
    petMicrochip VARCHAR(15), 				-- 반려동물 마이크로칩 번호
    petWeight DECIMAL(5, 2) NOT NULL, 		-- 반려동물 체중 (Kg 단위)
    petStatus ENUM('활성','비활성') NOT NULL DEFAULT '활성', -- 사용자 상태
    petImage VARCHAR(255),
    FOREIGN KEY (userNum) REFERENCES `USER` (userNum) ON DELETE CASCADE
);

-- 병원 관계자 테이블
CREATE TABLE MANAGER (
    managerNum INT PRIMARY KEY AUTO_INCREMENT, -- 병원 관계자(수의사,테크니션,매니저) 고유번호 (PK)
    managerName VARCHAR(100) NOT NULL, 			-- 병원 관계자 이름
    managerLicenseNum VARCHAR(5), 			-- 수의사 면허 번호
    managerId VARCHAR(50) NOT NULL UNIQUE, 		-- 병원 관계자 로그인 ID (고유값)
    managerPwd VARCHAR(100) NOT NULL, 			-- 병원 관계자 비밀번호 (암호화 필요)
    managerPhone VARCHAR(15) NOT NULL, 			-- 병원 관계자 연락처
    managerEmail VARCHAR(100) NOT NULL UNIQUE, 	-- 병원 관계자 이메일 (고유값)
    managerBirth DATE NOT NULL, 				-- 병원 관계자 생년월일
    managerGender ENUM('여성', '남성') NOT NULL, 	-- 병원 관계자 성별 (0: 여성, 1: 남성)
    managerAddress VARCHAR(50) NOT NULL, 		-- 병원 관계자 주소
    managerSignupDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    managerImage VARCHAR(255)
);

-- 병원 관계자 권한부여 테이블
CREATE TABLE `PERMISSION` (
    managerNum INT NOT NULL, 		-- 병원 관계자 고유번호 (FK)
    permissionRole ENUM('수의사','매니저','테크니션') NOT NULL, 	-- 역할 (관리자, 수의사, 매니저, 테크니션)
    permissionState ENUM('직원','퇴사') NOT NULL DEFAULT(1),
    permissionPasswordless ENUM('미등록', '등록') NOT NULL DEFAULT(1), -- 비밀번호 없이 로그인 가능한지 여부
    FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE
);

-- 회사 소개 테이블
CREATE TABLE NOTICE(
	noticeNum INT PRIMARY KEY AUTO_INCREMENT,
	noticeDate TIMESTAMP,
    noticeTitle VARCHAR(15), 		  -- 병원 연락처
    noticeContent VARCHAR(100), 		  -- 병원 이메일
    noticeImage VARCHAR(100), 		  -- 병원 운영시간
    noticeHit INT(10) NOT NULL, -- 병원 소개
    noticeStatus VARCHAR(1000) NOT NULL -- 병원에서 제공하는 서비스 설명
);

CREATE TABLE NOTICEIMAGE (
    noticeimageNum INT PRIMARY KEY AUTO_INCREMENT, -- 이미지 고유 번호
    noticeNum INT NOT NULL, -- NOTICE 테이블과 연결된 공지사항 번호
    noticeimageName VARCHAR(255),
    FOREIGN KEY (noticeNum) REFERENCES NOTICE(noticeNum) ON DELETE CASCADE
);

CREATE TABLE MFILE(
	mfileNum INT PRIMARY KEY AUTO_INCREMENT,
	mfileTitle VARCHAR(255) NOT NULL,
	mfileContent VARCHAR(255) NOT NULL,
	mfileDate TIMESTAMP,
	mfileModifiedDate TIMESTAMP,
	mfileDownloadCount INT(255) DEFAULT 0,
	mfileUploader VARCHAR(10),
	mfileName VARCHAR(255),
	mfileRealName VARCHAR(255),
	mfileCategory INT NOT NULL,
	mfileStatus ENUM('활성','비활성') NOT NULL DEFAULT(1)
);

-- user(사용자) 예약 테이블
CREATE TABLE RESERVE (
    reserveNum INT PRIMARY KEY AUTO_INCREMENT,  -- 예약 고유번호 (PK)
    petNum INT,                                 -- 반려동물 고유번호 (FK)
    managerNum INT,                             -- 병원 관계자(수의사) 고유번호 (FK)
    reserveStatus ENUM('대기','완료','취소') NOT NULL DEFAULT(1),-- 예약 상태 (예: 대기, 완료, 취소 등)
    reserveDate TIMESTAMP NOT NULL,                     -- 예약 날짜
    reserveNotice VARCHAR(500) NOT NULL,         -- 예약 관련 메모
    FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE,
    FOREIGN KEY (petNum) REFERENCES PET (petNum) ON DELETE CASCADE
);

-- 수의사 캘린더
CREATE TABLE CALENDAR (
    calendarNum INT PRIMARY KEY AUTO_INCREMENT, -- 캘린더 고유번호 (PK)
    managerNum INT NOT NULL,
    reserveNum INT,                      -- 병원 관계자(수의사) 고유번호 (FK)
    calendarStartTime TIMESTAMP NOT NULL,               -- 일정 시작 시간
    calendarEndTime TIMESTAMP,                  -- 일정 종료 시간
    calendarTitle VARCHAR(100),                    -- 일정 제목
    calendarMemo VARCHAR(200),                  -- 일정 관련 메모
    FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE,
    FOREIGN KEY (reserveNum) REFERENCES RESERVE (reserveNum) ON DELETE CASCADE
);

-- 위치 테이블
CREATE TABLE LOCATION (
    locationId INT PRIMARY KEY AUTO_INCREMENT,  -- 위치 고유 ID (자동 증가)
    locationName VARCHAR(255) NOT NULL UNIQUE   -- 위치 이름 (예: 수술실1, 진료실1 등)
);

-- 제품 테이블
CREATE TABLE ITEM (
    itemId VARCHAR(255) PRIMARY KEY,   -- 제품 고유 ID (자동 증가)
	itemCategory VARCHAR(255),  -- 제품 카테고리
    itemName VARCHAR(255) NOT NULL,           -- 제품 이름
    itemPrice INT,                           -- 제품 가격
    itemState ENUM('일반', '마약') DEFAULT '일반'  -- 제품 상태 (기본값 '일반')
);

-- 재고 이력 테이블 (제품의 입출고 기록을 추적)
CREATE TABLE ITEMHISTORY (
    historyNum INT PRIMARY KEY AUTO_INCREMENT,           -- 재고 이력 고유 ID (자동 증가)
    itemId VARCHAR(255),                                         -- 제품 ID (ITEM 테이블의 itemId와 연결)
    locationId INT,                                    -- 위치 ID (LOCATION 테이블의 locationId와 연결)
	historyQuantity INT NOT NULL,                        -- 재고 수량 (입고/출고된 수량)
	transactionType ENUM('입고', '출고') NOT NULL,       -- 트랜잭션 종류 ('입고' 또는 '출고')
    transactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 트랜잭션 발생 시간 (재고변동 시간)
    FOREIGN KEY (itemId) REFERENCES ITEM(itemId),        -- 제품 ID에 대한 외래 키 (ITEM 테이블 참조)
    FOREIGN KEY (locationId) REFERENCES LOCATION(locationId)  -- 위치 ID에 대한 외래 키 (LOCATION 테이블 참조)
);

-- 공급자 테이블 (제품과 공급자 정보를 저장)
CREATE TABLE SUPPLIER (
    supplierNum INT PRIMARY KEY AUTO_INCREMENT,     -- 공급자 고유 번호 (자동 증가)
    supplierName VARCHAR(255) NOT NULL,             -- 공급자 이름
    supplierCode VARCHAR(100) NOT NULL,             -- 공급자 코드
    supplierEmployee VARCHAR(30),                   -- 공급자 담당 직원
    managerNum INT,                                 -- 공급자 담당 매니저 번호 (추가 관리용)
    supplierTransactionId VARCHAR(30) UNIQUE,       -- 공급자 트랜잭션 ID (고유 식별자)
    itemId VARCHAR(255),                            -- 공급된 제품 ID (ITEM 테이블의 itemId와 연결)
    supplierProductWeight DECIMAL(8,2) NOT NULL,    -- 공급자 제품의 무게
    supplierProductQuantity INT NOT NULL,           -- 공급자 제품의 수량
    supplierExpirationDate DATE NOT NULL,           -- 공급자 제품의 유통기한
    FOREIGN KEY (itemId) REFERENCES ITEM(itemId),   -- 제품 ID에 대한 외래 키 (ITEM 테이블 참조)
    FOREIGN KEY (managerNum) REFERENCES MANAGER(managerNum)  -- 매니저에 대한 외래 키 (MANAGER 테이블 참조)
);

-- 환자 차트 테이블
CREATE TABLE CHART (
    chartNum INT PRIMARY KEY AUTO_INCREMENT, -- 차트 고유번호 (PK)
    itemId VARCHAR(255),
    reserveNum INT, 						  -- 예약 정보 (FK)
    chartNote VARCHAR(1000),				  -- 진료 기록
    chartVital INT(30), 					  -- 환자 활력징후
    chartDate DATE, 						  -- 차트 작성일
    chartFile VARCHAR(255), 				  -- 첨부된 파일 경로
    chartCode VARCHAR(15),
    FOREIGN KEY (itemId) REFERENCES ITEM (itemId) ON DELETE CASCADE,
    FOREIGN KEY (reserveNum) REFERENCES `RESERVE` (reserveNum) ON DELETE CASCADE
);

CREATE TABLE CHARTLOG (
    ChartlogNum INT PRIMARY KEY AUTO_INCREMENT,
    chartNum INT,
    petNum INT,
    chartlogDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chartNum) REFERENCES CHART (chartNum) ON DELETE CASCADE,
    FOREIGN KEY (petNum) REFERENCES PET (petNum) ON DELETE CASCADE
);

-- 방사선, 초음파, CT 등 의료용 사진 테이블
CREATE TABLE MEDICALFILE (
    medicalNum INT PRIMARY KEY AUTO_INCREMENT, -- 의료 파일 고유번호 (PK)
    managerNum INT NOT NULL, 						-- 보호자(사용자) 고유번호 (FK)
    chartNum INT, 								-- 관련된 차트 번호 (FK)
    medicalDate DATE, 				-- 촬영 날짜
    medicalNote VARCHAR(1000), 					-- 촬영 관련 메모
    studyUid VARCHAR(100),
    seriesUid VARCHAR(100),
    instanceUid VARCHAR(100),
    FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE,
    FOREIGN KEY (chartNum) REFERENCES CHART (chartNum) ON DELETE CASCADE
);

-- 챗봇AI 테이블
CREATE TABLE CHATAI(
    chatAiNum INT PRIMARY KEY AUTO_INCREMENT,-- 챗봇 AI 로그 번호 (PK)
    userNum INT NOT NULL,                        -- 사용자 고유번호 (FK)
    chatHistory JSON,                -- chat history를 json형식으로 저장
    chatAiDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- 대화 날짜
    FOREIGN KEY (userNum) REFERENCES USER (userNum) ON DELETE CASCADE
);

-- 입원 테이블
CREATE TABLE HOSPITALLOG(
	hospitalNum INT PRIMARY KEY AUTO_INCREMENT, -- 입원 기록 고유번호 (PK)
	chartNum INT,
	hospitalRoom INT NOT NULL,
	hospitalStartTime TIMESTAMP NOT NULL,
    hospitalMemo VARCHAR(500), 		 -- 입원 관련 메모
    hospitalStatus ENUM('활성','퇴원') NOT NULL DEFAULT(1), -- 환자 입원 상태
	FOREIGN KEY (chartNum) REFERENCES CHART (chartNum) ON DELETE CASCADE,
    UNIQUE (chartNum, hospitalRoom)
);

-- 사내메신저 톡방 테이블
CREATE TABLE CHATROOM(
	chatroomNum INT PRIMARY KEY AUTO_INCREMENT, -- 채팅방 고유번호 (PK)
	managerNum INT,
    chatroomName VARCHAR(50) NOT NULL, 		  -- 채팅방 이름
    chatroomGroup ENUM('개인','단체'), 			  -- 그룹 채팅 여부
    chatroomCreateAt DATE, 					  -- 채팅방 생성일
    FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE
);

-- 사내메신저 메세지 테이블
CREATE TABLE MESSAGE(
	messageNum INT PRIMARY KEY AUTO_INCREMENT, 	-- 메시지 고유번호 (PK)
    chatroomNum INT, 								-- 채팅방 고유번호 (FK)
    managerNum INT,								-- 보낸 사용자 고유번호 (FK)
    messageContent VARCHAR(3000) NOT NULL,
    messageCreatedAt TIMESTAMP,
    messagetype ENUM('텍스트','동영상','이미지'),		    -- 미디어 파일 유형 (예: 이미지, 동영상)
    messageState ENUM('읽음','보냄','실패'),
	FOREIGN KEY (chatroomNum) REFERENCES CHATROOM (chatroomNum) ON DELETE CASCADE,
	FOREIGN KEY (managerNum) REFERENCES MANAGER (managerNum) ON DELETE CASCADE
);

-- 맴버 관리 테이블
CREATE TABLE CHATROOMMEMBER (
    chatroomNum INT,       -- 채팅방 고유번호 (CHATROOM 테이블과 연결)
    managerNum INT,        -- 채팅방 참여자 번호 (MANAGER 테이블과 연결)
    joinDate DATE,         -- 채팅방 참여일
    PRIMARY KEY (chatroomNum, managerNum),  -- 채팅방과 참여자는 고유한 관계
    FOREIGN KEY (chatroomNum) REFERENCES CHATROOM(chatroomNum) ON DELETE CASCADE,
    FOREIGN KEY (managerNum) REFERENCES MANAGER(managerNum) ON DELETE CASCADE
);

CREATE TABLE LOG (
    logNum INT AUTO_INCREMENT PRIMARY KEY,  -- 로그 고유번호 (PK)
    logCategory ENUM('INFO', 'ERROR') NOT NULL,  -- 로그 카테고리
    logLocation VARCHAR(20) NOT NULL,  -- 발생 위치
    logDetail TEXT NOT NULL,  -- 상세 로그 (stackTrace 등)
    logRemoteAddr VARCHAR(20) NULL,  -- IP 주소 (NULL 허용)
    logDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 로그 날짜 (자동 생성)
);

-- 게시판 테이블 (Upboard)
CREATE TABLE UPBOARD (
    upboardNum INT AUTO_INCREMENT PRIMARY KEY, -- 게시글 번호 (기본 키, 자동 증가)
    userNum int,
    upboardTitle VARCHAR(255) NOT NULL, -- 제목
    upboardContent TEXT, -- 내용 (긴 텍스트 저장)
    upboardImgn VARCHAR(255), -- 이미지 파일명
    upboardHit INT DEFAULT 0, -- 조회수 (기본값 0)
    upboardReip VARCHAR(50), -- 작성자 IP
    upboardBdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 등록 날짜 (기본값 현재 시간)
    FOREIGN KEY (userNum) REFERENCES USER (userNum) ON DELETE CASCADE
);

-- 댓글 테이블 (UpboardComm)
CREATE TABLE UPBOARDCOMM (
    upboardCommNum INT AUTO_INCREMENT PRIMARY KEY, -- 댓글 번호 (기본 키)
    upboardNum INT NOT NULL, -- 게시글 번호 (외래 키)
    parentNum INT NULL, -- 부모 댓글 번호 (대댓글을 위한 컬럼)
    upboardCommState ENUM('활성', '비활성') DEFAULT '활성', -- 댓글 상태
    upboardCommContent TEXT, -- 댓글 내용
    upboardCommReip VARCHAR(50), -- 작성자 IP
    upboardCommDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 작성 날짜 (기본값 현재 시간)
    userNum INT NOT NULL,
    FOREIGN KEY (UpboardNum) REFERENCES UPBOARD (UpboardNum) ON DELETE CASCADE,
    FOREIGN KEY (userNum) REFERENCES USER(userNUm) ON DELETE CASCADE
);

CREATE TABLE PAYMENTREQUEST (
    paymentId VARCHAR(255) PRIMARY KEY,                    -- 결제 고유 ID
    orderId VARCHAR(255) NOT NULL UNIQUE,                             -- 주문 ID (유니크)
    paymentMethod ENUM('가상계좌', '간편결제', '게임문화상품권', '계좌이체', '도서문화상품권', '문화상품권', '카드', '휴대폰') NOT NULL, -- 결제 방법
    paymentStatus ENUM('중단됨', '취소됨', '완료됨', '만료됨', '진행중', '부분 취소됨', '준비됨', '입금 대기중') NOT NULL, -- 결제 상태
    amount BIGINT NOT NULL,                                      -- 결제 금액
    approvedAt TIMESTAMP DEFAULT NULL,                              -- 결제 승인 일시
    requestedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,                                 -- 결제 요청 일시
    userNum INT NOT NULL,                                           -- 사용자 번호 (USER 테이블의 외래 키)
    reserveNum INT NOT NULL,
    paymentKey VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY (userNum) REFERENCES USER(userNum),                 -- 사용자 테이블과 연결
    FOREIGN KEY (reserveNum) REFERENCES RESERVE(reserveNum)
);

CREATE TABLE PAYMENTCHECK (
	id INT AUTO_INCREMENT PRIMARY KEY,
    mse DOUBLE,
    normalAboveThreshold DOUBLE,
    anomalyProbability DOUBLE,
    isAnomaly BOOLEAN,
    userIP VARCHAR(200),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    userNum INT NOT NULL, 
    FOREIGN KEY (userNum) REFERENCES USER(userNum)
);