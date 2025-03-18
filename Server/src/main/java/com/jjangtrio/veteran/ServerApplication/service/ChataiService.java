package com.jjangtrio.veteran.ServerApplication.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjangtrio.veteran.ServerApplication.dao.ChataiDAO;

@Service
public class ChataiService {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    @Autowired
    private ChataiDAO chataiDAO;

    // private final String apiUrl = "https://api.openai.com/v1/chat/completions";

    private final String Url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // gemini-2.0-flash
    public String getChatbotResponse(String userMessage) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type: application/json";

        String prompt = "너는 동물병원 고객 상담 챗봇이야.\n\n" +

                "### 너의 역할\n" +
                "1. 반려동물의 증상을 듣고 분석해서 어떤 문제가 있는지 유추해.\n" +
                "2. 응답은 **최대 3문장**으로 간결하게 대답해.\n" +
                "3. 동물의 나이와 종이 중요하다면 먼저 물어보고, 추가 정보를 바탕으로 건강 상태를 진단해.\n" +
                "4. 증상 정보가 부족하면, **반드시** 추가 질문을 해서 정보를 보충해.\n\n" +

                "### 사용자의 질문 인식\n" +
                "1. '사용자의 질문'은 사용자가 직접 입력한 메시지를 말해. 예: '강아지가 아파요' 또는 '병원 예약하고 싶어요'.\n" +
                "2. 사용자의 질문에서 **'병원', '예약', '진료', '상담'** 중 하나의 단어가 포함된 경우에만 진료 예약을 안내해.\n" +
                "3. 사용자의 질문을 정확하게 이해하고, 그에 맞는 진료과(내과, 외과, 안과)를 유추한 후 안내해.\n\n" +

                "### 진료 예약 안내 형식\n" +
                "**- 진료 예약 안내 문장은 아래 예시처럼 출력해!**\n" +
                "'해당 증상은 **내과** 전문가 상담이 필요할 가능성이 높아요.\n' +\n" +
                "'진료를 원하시면 로그인 후 **[진료 예약하기]** 버튼을 눌러주세요.'\n" +
                "'더 궁금한 점이 있으신가요?'\n\n" +

                "### 추가 지침\n" +
                "- 문장마다 한 줄 띄어서 가독성을 높여.\n" +
                "- **진료과 명칭(내과, 외과, 안과)은 볼드체**로 표시해.\n" +
                "- **'진료 예약하기'와 '진료예약'은 볼드체**로 강조해.\n" +
                "- 마지막 문장을 가장 중요하게 다뤄서 강조하고, 질문 형태로 마무리하되 **명확하게** 답을 유도하도록 해.";

        String jsonInputString = "{\n" +
                "  \"contents\": [\n" +
                "    {\"parts\":[{\"text\": \"" + prompt + userMessage + "\"}]}\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(jsonInputString, headers);

        // curl
        // "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=GEMINI_API_KEY"
        // \
        // -H 'Content-Type: application/json' \
        // -X POST \
        // -d '{
        // "contents": [{
        // "parts":[{"text": "아파"}]
        // }]
        // }'

        String apiUrl = Url + "?key=" + apiKey;
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);                                                                                                          
            // System.out.println(response.getBody());
            // {
            // "candidates": [
            // {
            // "content": {
            // "parts": [
            // {
            // "text": "안녕하세요! 무엇을 도와드릴까요?\n"
            // }
            // ],
            // "role": "model"
            // },
            // "finishReason": "STOP",
            // "avgLogprobs": -0.020730778574943542
            // }
            // ],
            // "usageMetadata": {
            // "promptTokenCount": 2,
            // "candidatesTokenCount": 16,
            // "totalTokenCount": 18,
            // "promptTokensDetails": [
            // {
            // "modality": "TEXT",
            // "tokenCount": 2
            // }
            // ],
            // "candidatesTokensDetails": [
            // {
            // "modality": "TEXT",
            // "tokenCount": 16
            // }
            // ]
            // },
            // "modelVersion": "gemini-2.0-flash"
            // }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            String aiResponse = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return aiResponse;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean saveChatHistory(Long userNum, List<Map<String, Object>> chatHistory) {
        try {
            // [
            // {sender=나, content=우리 아이가 안과질환이 있어요},
            // {sender=멍트리오, content=궁금한 내용을 물어보세요!}
            // ]

            
            List<Map<String, String>> chatHistoryList = new ArrayList<>();
            
            for (Map<String,Object> chatMap : chatHistory) {

                Map<String, String> map = new HashMap<>();
                map.put("sender",chatMap.get("sender").toString());
                String originalString =chatMap.get("content").toString();
                map.put("content",originalString.replace("\n", "\\n"));
                chatHistoryList.add(map);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String chatHistoryJson = objectMapper.writeValueAsString(chatHistoryList);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userNum", userNum);
            resultMap.put("chatHistoryJson", chatHistoryJson);

            System.out.println(resultMap);
            chataiDAO.saveChatHistory(resultMap);

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getChatHistoryOneDay(Map<String, Object> map) {

    //    historyList :  [
    // {
    //     "chatAiDate": 1741507036000,
    //     "chatHistory": "[{\"sender\": \"나\", \"content\": \"우리 아이가 구토를 했어요\"}, {\"sender\": \"멍트리오\", \"content\": \"구토의 원인은 다양합니다.\\\\n\\\\n강아지의 나이와 구토 외 다른 증상(설사, 식욕 부진 등)이 있는지 알려주시면 더 정확한 원인을 파악하는 데 도움이 됩니다.\\\\n\\\\n추가 정보를 알려주시겠어요?\\\\n\"}]"
    // },
    // {
    //     "chatAiDate": 1741507081000,
    //     "chatHistory": "[{\"sender\": \"나\", \"content\": \"밥도 잘 안먹고 산책도 잘 안가요\"}, {\"sender\": \"멍트리오\", \"content\": \"어떤 동물이고, 나이가 어떻게 되나요?\\\\n\\\\n밥을 안 먹고 산책을 안 가는 것 외에 다른 증상(구토, 설사, 기침 등)은 없나요?\\\\n\"}]"
    // }, ....

        try {

            List<Map<String, Object>> historyList = chataiDAO.getChatHistoryByDate(map);
            List<Map<String, Object>> historyJson = new ArrayList<>();

            ObjectMapper objectMapper = new ObjectMapper();

            for (Map<String,Object> chat : historyList) {
               String jsonString = chat.get("chatHistory").toString();
               System.out.println(chat.get("chatHistory"));

               List<Map<String, Object>> parsedChatHistory = objectMapper.readValue(
                jsonString, new TypeReference<List<Map<String, Object>>>() {});
                historyJson.addAll(parsedChatHistory);
            }
            System.out.println(historyJson);

            return historyJson;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<String> getChatDateInfo(Long userNum) {
        try{

        List<Timestamp> chatAiDateList = chataiDAO.getChatDateInfo(userNum);
        List<String> dateList = new ArrayList<>();

        for (Timestamp chatAiDate : chatAiDateList) {
            Date date = new Date(chatAiDate.getTime());
            System.out.println("Date: " + date);
            dateList.add(date.toString());
        }

        return dateList;

        } catch (Exception e) {
            return null;
        }
    }


    public String chatSummary(List<Map<String, Object>> chatOriginalList) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Content-Type: application/json";

        String prompt = "너는 채팅 요약 전문가야."+
                        "채팅은 다음과 같이 { sender: 나, content: 우리 아이가 구토를 했어요} 형식의 리스트로 구성되어있어" +
                        "여기서 '나'는 반려동물의 보호자이고, '멍트리오'는 수의사야" +
                        "먼저 보호자가 말한 내용을 요약한 후 정리해서 제시해줘" +
                        "그 다음 수의사가 말한 내용을 요약해서 제시해줘" +
                        "너가 만드는 요약본은 반려동물의 보호자에게 전달되는 메세지야" +
                        "보호자가 채팅을 한번에 이해할 수 있도록 요약을 해줘.";

                        String jsonInputString = "{\n" +
                        "  \"contents\": [\n" +
                        "    {\"parts\":[{\"text\": \"" + prompt + chatOriginalList + "\"}]}\n" +
                        "  ]\n" +
                        "}";

        HttpEntity<String> request = new HttpEntity<>(jsonInputString, headers);

        String apiUrl = Url + "?key=" + apiKey;
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);                                                                                                          

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            String aiResponse = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return aiResponse;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // gpt-4o-mini
    // public String getChatbotResponse(String userMessage) {

    // RestTemplate restTemplate = new RestTemplate(); // Spring Boot에서 HTTP 요청을 보낼
    // 때 사용하는 객체

    // HttpHeaders headers = new HttpHeaders(); // HTTP 요청의 헤더 설정

    // headers.setContentType(MediaType.APPLICATION_JSON);
    // headers.set("Authorization", "Bearer " + apiKey);
    // /*
    // Content-Type : application/json
    // Authorization : Bearer + sk-proj-E*************************
    // */

    // Map<String, Object> requestBody = new HashMap<>(); // OpenAI API 요청 데이터
    // (body/raw)
    // requestBody.put("model", "gpt-4o-mini");
    // requestBody.put("messages", List.of(
    // Map.of("role", "system", "content",
    // "너는 동물병원 고객 상담 챗봇이야.\n\n" +

    // "### 너의 역할\n" +
    // "1. 반려동물의 증상을 듣고 분석해서 어떤 문제가 있는지 유추해.\n" +
    // "2. 응답은 **최대 3문장**으로 간결하게 대답해.\n" +
    // "3. 동물의 나이와 종이 중요하다면 먼저 물어보고, 추가 정보를 바탕으로 건강 상태를 진단해.\n" +
    // "4. 증상 정보가 부족하면, **반드시** 추가 질문을 해서 정보를 보충해.\n\n" +

    // "### 사용자의 질문 인식\n" +
    // "1. '사용자의 질문'은 사용자가 직접 입력한 메시지를 말해. 예: '강아지가 아파요' 또는 '병원 예약하고 싶어요'.\n" +
    // "2. 사용자의 질문에서 **'병원', '예약', '진료', '상담'** 중 하나의 단어가 포함된 경우에만 진료 예약을 안내해.\n" +
    // "3. 사용자의 질문을 정확하게 이해하고, 그에 맞는 진료과(내과, 외과, 안과)를 유추한 후 안내해.\n\n" +

    // "### 진료 예약 안내 형식\n" +
    // "**- 진료 예약 안내 문장은 아래 예시처럼 출력해!**\n" +
    // "'해당 증상은 **내과** 전문가 상담이 필요할 가능성이 높아요.\n' +\n" +
    // "'진료를 원하시면 로그인 후 **[진료 예약하기]** 버튼을 눌러주세요.'\n" +
    // "'더 궁금한 점이 있으신가요?'\n\n" +

    // "### 추가 지침\n" +
    // "- 문장마다 한 줄 띄어서 가독성을 높여.\n" +
    // "- **진료과 명칭(내과, 외과, 안과)은 볼드체**로 표시해.\n" +
    // "- **'진료 예약하기'와 '진료예약'은 볼드체**로 강조해.\n" +
    // "- 마지막 문장을 가장 중요하게 다뤄서 강조하고, 질문 형태로 마무리하되 **명확하게** 답을 유도하도록 해."
    // ),
    // Map.of("role", "user", "content", "마지막 문장을 중점으로 대답해줘" + userMessage)
    // ));
    // // requestBody.put("max_tokens", 200);
    // requestBody.put("store", true);

    // /*
    // 출처 : https://platform.openai.com/docs/api-reference/chat
    // {
    // "model": "gpt-4o-mini", // 사용할 모델 (gpt-4o-mini)
    // "messages": [ // 대화 내용 (역할과 메시지 포함)
    // {
    // "role": "system",
    // "content": "너는 동물병원 고객 상담 챗봇이야."
    // },
    // {
    // "role": "user",
    // "content": "우리 강아지가 밥을 안 먹어요. 넘나 고민되는것 ㅜㅜㅠㅠㅠ" // -> userMessage
    // }
    // ],
    // "max_tokens": 100, // 최대 응답 길이 (최대 4096 토큰)
    // "temperature": 0.7, // 창의성 조절 (0: 보수적, 1: 랜덤)
    // "store" : true, // 응답 저장 여부 (openai 서버에 저장)
    // "top_p": 1, // 확률 분포 기반 응답 샘플링 (1.0이면 무제한)
    // "n": 1, // 응답 개수 (한 번에 여러 개 받을 수도 있음)
    // "stream": false, // 스트리밍 여부 (실시간 응답 받을지 여부)
    // "stop": null, // 응답을 멈출 키워드 (["끝", "stop"] 등 설정 가능)
    // "presence_penalty": 0, // 새로운 주제를 더 많이 생성하도록 유도 (-2.0 ~ 2.0)
    // "frequency_penalty": 0,
    // "logit_bias": null,
    // "user": "user-1234"
    // }
    // */

    // HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody,
    // headers);
    // ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST,
    // request, Map.class); // 응답을 저장

    // /* 응답 예시
    // {
    //     "id": "chatcmpl-B1rOqgiNKh3gAqXOQdcSPm9kI7jon", // 요청시 store : ture인 경우 위
    // id로 조회 및 삭제 가능
    //     "object": "chat.completion",
    //     "created": 1739783296,
    //     "model": "gpt-4o-mini-2024-07-18", // service return 하는 내용은
    // response.getBody() 에서
    //     "choices": [ // .get("choices")
    //         {
    //             "index": 0, // .get(0)
    //             "message": { // .get("message").get("content").toString();
    //                 "role": "assistant",
    //                 "content": "강아지가 밥을 안 먹는 것은 ... 어쩌구 저쩌구",
    //                 "refusal": null
    //             },
    //             "logprobs": null,
    //             "finish_reason": "stop"
    //         }
    //     ],
    //     "usage": {
    //         "prompt_tokens": 103,
    //         "completion_tokens": 91,
    //         "total_tokens": 194,
    //         "prompt_tokens_details": {
    //             "cached_tokens": 0,
    //             "audio_tokens": 0
    //         },
    //         "completion_tokens_details": {
    //             "reasoning_tokens": 0,
    //             "audio_tokens": 0,
    //             "accepted_prediction_tokens": 0,
    //             "rejected_prediction_tokens": 0
    //         }
    //     },
    //     "service_tier": "default",
    //     "system_fingerprint": "fp_13eed4fce1"
    // }
    // */
    // List<Map<String, Object>> choices = (List<Map<String,
    // Object>>)response.getBody().get("choices");
    // Map<String, Object> message = (Map<String,
    // Object>)choices.get(0).get("message");
    // String aiResponse = message.get("content").toString();

    // return aiResponse;
    // }

    // public boolean saveChatHistory(Long userNum, List<Map<String, Object>>
    // chatList) {
    // try {
    // // [
    // // {sender=멍트리오, content=궁금한 내용을 물어보세요!},
    // // {sender=나, content=우리 아이가 안과질환이 있어요},
    // // {sender=멍트리오, content=강아지가 ....}]
    // // ]
    // int chatSize = chatList.size();
    // for (int i = 0; i < chatSize; i++) {
    // Map<String, Object> chat = chatList.get(i);
    // String sender = (String) chat.get("sender");
    // String content = (String) chat.get("content");

    // }

    // // chataiDAO.saveChatHistory(userNum, chatList);
    // return true;
    // } catch (Exception e) {
    // return false;
    // }
    // }
}
