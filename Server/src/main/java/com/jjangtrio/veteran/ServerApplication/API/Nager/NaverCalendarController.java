package com.jjangtrio.veteran.ServerApplication.API.Nager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/naver")
public class NaverCalendarController {

    String token = "AAAAPbVFHTJ2JV_-9jqdBrXtnjWwbBesTO9CkmpQZ20OFC3IYCMpEpdDl3tDzuP9q5AYOzrxg7D9Y_hPzbmtlcrdpvY";
    String header = "Bearer " + token;

    @PostMapping("/calendar")
    public void calendar() {
        try {
            String apiURL = "https://openapi.naver.com/calendar/createSchedule.json";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", header);
            String calSum = URLEncoder.encode("[제목] 캘린더API로 추가한 일정", "UTF-8");
            String calDes = URLEncoder.encode("[상세] 회의 합니다", "UTF-8");
            String calLoc = URLEncoder.encode("[장소] 그린팩토리", "UTF-8");
            String uid = UUID.randomUUID().toString();
            String scheduleIcalString = "BEGIN:VCALENDAR\n" +
                    "VERSION:2.0\n" +
                    "PRODID:Naver Calendar\n" +
                    "CALSCALE:GREGORIAN\n" +
                    "BEGIN:VTIMEZONE\n" +
                    "TZID:Asia/Seoul\n" +
                    "BEGIN:STANDARD\n" +
                    "DTSTART:19700101T000000\n" +
                    "TZNAME:GMT%2B09:00\n" +
                    "TZOFFSETFROM:%2B0900\n" +
                    "TZOFFSETTO:%2B0900\n" +
                    "END:STANDARD\n" +
                    "END:VTIMEZONE\n" +
                    "BEGIN:VEVENT\n" +
                    "SEQUENCE:0\n" +
                    "CLASS:PUBLIC\n" +
                    "TRANSP:OPAQUE\n" +
                    "UID:" + uid + "\n" + // 일정 고유 아이디
                    "DTSTART;TZID=Asia/Seoul:20161116T170000\n" + // 시작 일시
                    "DTEND;TZID=Asia/Seoul:20161116T173000\n" + // 종료 일시
                    "SUMMARY:" + calSum + " \n" + // 일정 제목
                    "DESCRIPTION:" + calDes + " \n" + // 일정 상세 내용
                    "LOCATION:" + calLoc + " \n" + // 장소
                    "RRULE:FREQ=YEARLY;BYDAY=FR;INTERVAL=1;UNTIL=20201231\n" + // 일정 반복시 설정
                    "ORGANIZER;CN=관리자:mailto:admin@sample.com\n" + // 일정 만든 사람
                    "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;CN=admin:mailto:user1@sample.com\n" + // 참석자
                    "CREATED:20161116T160000Z\n" + // 일정 생성시각
                    "LAST-MODIFIED:20161116T160000Z\n" + // 일정 수정시각
                    "DTSTAMP:20161116T160000Z\n" + // 일정 타임스탬프
                    "END:VEVENT\n" +
                    "END:VCALENDAR";
            String postParams = "calendarId=defaultCalendarId&scheduleIcalString=" + scheduleIcalString;
            System.out.println(postParams);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
