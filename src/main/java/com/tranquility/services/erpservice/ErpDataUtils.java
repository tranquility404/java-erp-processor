package com.tranquility.services.erpservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.models.Cookie;
import com.tranquility.models.ErpScrapedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErpDataUtils {

    private ErpScrapedData erpScrapedData;

    public ErpDataUtils(ErpScrapedData erpScrapedData) {
        this.erpScrapedData = erpScrapedData;
    }

    public static void main(String[] args) {
//        ObjectMapper objectMapper = new ObjectMapper();

    }

    public String createAttendanceBody(Map<String, Object> attendanceMap) {
        StringBuilder attendanceBody = new StringBuilder("{ ");
        int l = 0;
        for (Map.Entry<String, Object> e : attendanceMap.entrySet()) {
            attendanceBody.append("\"");
            attendanceBody.append(e.getKey());
            attendanceBody.append("\":\"");
            attendanceBody.append(e.getValue());
            l = attendanceBody.length();
            attendanceBody.append("\", ");
        }
        attendanceBody.delete(l+1, attendanceBody.length());
        attendanceBody.append(" }");
        System.out.println(attendanceBody);
        return attendanceBody.toString();
    }

    public HashMap<String, String> convertToHashMap(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        String studentDataJsonString = rootNode.path("studentData").asText();
        return mapper.readValue(studentDataJsonString, new TypeReference<>() {});
    }

    public Object getResponseMap(String body) {
//        Return d Object
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map = mapper.readValue(body, Map.class);
            if (map.containsKey("d")) {
                return map.get("d");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public List<Cookie> extractCookiesMap(Map<String, List<String>> responseHeaders, String[] cookieKeysArray) {
        ArrayList<Cookie> cookieList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            for (String value : entry.getValue()) {
                if (entry.getKey().contains("set-cookie")) {

                    for (String cookiekey : cookieKeysArray) {
                        int start = 0;
                        if (value.contains(cookiekey)) {
                            int end1 = value.indexOf(";", start);
                            String cookie = value.substring(0, end1);
                            String expiry = extractExpiry(value, end1);

//                            System.out.println(cookie + ": " + expiry);

                            cookieList.add(new Cookie(cookie, expiry));
                        }
                    }
                }
            }
        }

        return cookieList;
    }

    public String extractExpiry(String value, int prevEnd) {
        int start = 0;
        if (prevEnd + 1 < value.length())
            start = prevEnd + 1;

        if (value.contains("expires=")) {
            int expDateStart = value.indexOf("expires=", start) + "expires=".length();
            return value.substring(expDateStart, value.indexOf(";", start));
        }

        return null;
    }

    public ArrayList<Cookie> transformToCookieResponse(HashMap<String, Map<String, String>> cookies) {
        ArrayList<Cookie> res = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : cookies.entrySet()) {
            res.add(new Cookie(e.getValue().get("cookie"), e.getValue().get("expiry")));
        }
        return res;
    }

    public Cookie transformToCaptchaData(HashMap<String, String> captchaData) {
        return new Cookie(captchaData.get("cookie"), captchaData.get("expiry"));
    }

    public ArrayList<Map<String, String>> transformToHashMap(ArrayList<Cookie> cookieResponse) {
        ArrayList<Map<String, String>> cookieList = new ArrayList<>();

        for (Cookie cookie : cookieResponse) {
            Map<String, String> map = new HashMap<>();
            map.put("cookie", cookie.getCookie());
            map.put("expiry", cookie.getExpiry());
            cookieList.add(map);
        }
        return cookieList;
    }
}
