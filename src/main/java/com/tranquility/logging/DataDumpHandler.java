package com.tranquility.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.utils.Utils;
import com.tranquility.models.Cookie;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DataDumpHandler {

    //    Directories
    private String DATA_DUMP, LOGS, DATA;

    //    Log Files
    private final String sessionCookies = "session-cookies.json",
            captchaCookies = "captcha-cookie.json",
            loginCookies = "login-cookies.json",
            roleIdReq = "role-id-req.json",
            chooseRoleCookies = "choose-role-cookies.json",
            studentData = "student-data.json",
            attendanceData = "attendance-data-json";


    private String outputFile = "output",
            userProfile = "user-profile.json";

    public DataDumpHandler(String username, String pass) throws IOException {
        DATA_DUMP = "/backend-app/data-dump/" + username;
        LOGS = DATA_DUMP + "/logs/";
        DATA = DATA_DUMP + "/data/";

        Files.createDirectories(Paths.get(LOGS));
        Files.createDirectories(Paths.get(DATA));
    }

    public void dumpSessionCookiesReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap) {
        saveRequestData(new File(LOGS, sessionCookies), requestMap, responseMap);
//        saveJson(new File(DATA, sessionCookies), data);
    }

    public void dumpCaptchaReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, Cookie data) {
        saveRequestData(new File(LOGS, captchaCookies), requestMap, responseMap);
//        saveJson(new File(DATA, captchaCookies), data);
    }

    public void dumpLoginCookiesReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, String requestBody, String responseBody, ArrayList<Cookie> data, String choseRoleUrl) {
        Map<String, List<String>> map1 = new HashMap<>(requestMap);
        Map<String, List<String>> map2 = new HashMap<>(responseMap);

        map1.put("request-body", Collections.singletonList(requestBody));
        map2.put("response-body", Collections.singletonList(responseBody));

        saveRequestData(new File(LOGS, loginCookies), map1, map2);
//        saveJson(new File(DATA, loginCookies), data);
//        updateUserProfile("choose-role-url", choseRoleUrl);
    }

    public void dumpChooseRoleCookiesReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, String responseBody, ArrayList<Cookie> data, String dashboardUrl) {
        Map<String, List<String>> map2 = new HashMap<>(responseMap);
        map2.put("response-body", Collections.singletonList(responseBody));

        saveRequestData(new File(LOGS, chooseRoleCookies), requestMap, map2);
//        saveJson(new File(DATA, chooseRoleCookies), data);
//        updateUserProfile("dashboard-url", dashboardUrl);
    }

    public void dumpRoleIdReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, String responseBody, String roleId) {
        Map<String, List<String>> map2 = new HashMap<>(responseMap);
        map2.put("response-body", Collections.singletonList(responseBody));

        saveRequestData(new File(LOGS, roleIdReq), requestMap, map2);
        updateUserProfile("role-id", roleId);
    }

    public void dumpStudentDataReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, String responseBody, Map<String, Object> data) {
        Map<String, List<String>> map2 = new HashMap<>(responseMap);
        map2.put("response-body", Collections.singletonList(responseBody));

        saveRequestData(new File(LOGS, studentData), requestMap, map2);
        saveJson(new File(DATA, studentData), data);
    }

    public void dumpAttendanceReqRes(Map<String, List<String>> requestMap, Map<String, List<String>> responseMap, String responseBody) {
        Map<String, List<String>> map2 = new HashMap<>(responseMap);
        map2.put("response-body", Collections.singletonList(responseBody));

        saveRequestData(new File(LOGS, attendanceData), requestMap, map2);
    }

    private void saveRequestData(File file, Map<String, List<String>> requestMap, Map<String, List<String>> responseMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("request-time", Collections.singletonList(Utils.getCurrentTime()));
        map.put("request", requestMap);
        map.put("response", responseMap);

        saveJson(file, map);
    }

    private void saveJson(File file, Object map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, map);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateUserProfile(String key, String data) {
        File file = new File(DATA, userProfile);
    }

    public void saveOutput(String text) {
        String formattedText = Utils.getCurrentTime() + ":\n" + text + "\n\n";
        prependTextToFile(new File(DATA_DUMP, outputFile + ".txt"), formattedText);
    }

    private void prependTextToFile(File originalFile, String text) {
        File tempFile = new File(DATA_DUMP, originalFile.getName() + System.currentTimeMillis() + ".txt");
        checkFile(new File[]{originalFile, tempFile});

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            writer.write(text);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (originalFile.delete()) {
            tempFile.renameTo(originalFile);
            System.out.println("File updated successfully.");
        } else {
            System.out.println("Failed to delete the original file.");
        }
    }

    private void checkFile(File[] files) {
        for (File f : files)
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
    }

//    public void saveCaptchaImg(byte[] data) {
//        Utils.saveFileData(data, new File(DATA_DUMP, captchaImgFile));
//    }
}
