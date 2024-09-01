package com.tranquility.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.data.MongoRepoService;
import com.tranquility.helpers.Utils;
import com.tranquility.model.*;
import com.tranquility.scrappers.ScrapeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ErpDataHandler {

    private final int CAPTCHA_RETRY_LIMIT = 10;
    private int captchaReadAttempt = 0;
    private boolean isLoginCookiesSecured = false;

    private String username;
    private String pass;

    private final String HOST_URL = "https://erp.axiscolleges.net/";
    private final String MAIN_URL = "index.aspx?openFor=Students&institute=ab0d4edc-07f0-45de-aac9-9917b3ea16ef";
    private final String CAPTCHA_URL = "captchaHandler.ashx?query=";
    private final String LOGIN_URL = "json/UserServiceWS.asmx/AuthenticateUser";
    private final String CHOOSE_ROLE_URL = "chooseURole.aspx/btnNext_Click";
    private final String ROLE_ID_URL = "chooseURole.aspx/getRoles";
    private final String STUDENT_DATA_URL = "home.aspx/getStuDashboard_Details";
    private final String ATTENDANCE_URL = "viewStuAttendance_rpt.aspx/getAttendance";
    private final String SUBJECTS_URL = "home.aspx/getMySubjects";
    private final String CLASSMATES_URL = "home.aspx/getClassMates";
    private final String CIRCULARS_URL = "home.aspx/btnSearch_NoticeCirculars";
    private final String ACADEMIC_CALENDAR_URL = "json/controlBindService.asmx/getAcademicCalendarItems";

    private String flaskServerUrl;

    private ErpData erpData;
    private ErpDataUtils erpDataUtils;
    private HttpClient client = HttpClient.newHttpClient();
//    private DataDumpHandler dataDumpHandler;

    @Autowired
    private MongoRepoService mongoRepoService;

    public ErpDataHandler(@Value("${flask.server.url}") String flaskServerUrl) {
        this.flaskServerUrl = flaskServerUrl;
    }

    public synchronized void initialize(String username, String pass) {
        this.username = username;
        this.pass = pass;

        this.erpData = ErpData.Companion.getInstance(username);
//            this.dataDumpHandler = new DataDumpHandler(username, pass);
        this.erpDataUtils = new ErpDataUtils(erpData);
    }

    public List<Map<String, String>> getAcademicCalendar() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ACADEMIC_CALENDAR_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{\"calendarId\":\"5\"}"))
                .headers(erpData.getHeaders(erpData.getCookiesForSubjects(), erpData.getUserProfile().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        String html = mapper.readTree(response.body()).path("d").asText();

        System.out.println("End of Get Academic Calendar");
        return ScrapeHelper.extractAcademicCalendar(html);
    }

    public List<Circular> getCirculars() throws IOException, InterruptedException {
        int[] date = Utils.getCurrentMonthAndYear();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + CIRCULARS_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpData.getBodyForCirculars(date[0], date[1])))
                .headers(erpData.getHeaders(erpData.getCookiesForSubjects(), erpData.getUserProfile().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("End of Get Circulars");
        return ScrapeHelper.extractCirculars(response.body());
    }

    public List<Classmate> getClassmates() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + CLASSMATES_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpData.getHeaders(erpData.getCookiesForSubjects(), erpData.getUserProfile().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        String html = mapper.readTree(response.body()).path("d").asText();

        System.out.println("End of Get Classmates");
        return ScrapeHelper.extractClassmates(html);
    }

    public List<Subject> getSubjects() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + SUBJECTS_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpData.getHeaders(erpData.getCookiesForSubjects(), erpData.getUserProfile().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        String html = mapper.readTree(response.body()).path("d").asText();

        System.out.println("End of Get Subjects");
        return ScrapeHelper.extractSubjects(html);
    }

    public String getAttendance() throws IOException, InterruptedException {
        HashMap<String, String> studentData = erpDataUtils.convertToHashMap(mongoRepoService.getUserDataRepo().findStudentDataById(username));
        System.out.println(erpData.getBodyForAttendance(studentData));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ATTENDANCE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpData.getBodyForAttendance(studentData)))
                .headers(erpData.getHeaders(erpData.getCookiesForDashBoard(), erpData.getUserProfile().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String attendanceData = (String) erpDataUtils.getResponseMap(response.body());

        System.out.println("End of Get Attendance");
        return attendanceData;
    }

    public Map<String, Object> getStudentData() throws IOException, InterruptedException {
        mongoRepoService.getUserDataRepo().save(UserData.Companion.instance(username));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + STUDENT_DATA_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpData.getHeaders(erpData.getCookiesForDashBoard(), erpData.getUserProfile().getChooseUserRefererUrl(), "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("student-data-status: " + response.statusCode());

        System.out.println(response.body());
        ArrayList list = (ArrayList) erpDataUtils.getResponseMap(response.body());

        if (!list.isEmpty()) {
            Map<String, Object> studentData = (Map<String, Object>) list.get(0);
            String jsonString = new ObjectMapper().writeValueAsString(studentData);
            mongoRepoService.updateStudentData(username, jsonString);
            return studentData;
        }

        System.out.println("End of Get-Student-Data");
        return null;
    }


    //      -----------------LOGIN FUNCTIONS-------------------------------------------
    public void loginIfExpired() throws IOException, InterruptedException {
        String expiry = mongoRepoService.getFirstLoginCookieExpiry(username);

        if (expiry != null && Utils.calculateRemainingTime(expiry) > 1) {
            System.out.println("Expiry: " + Utils.calculateRemainingTime(expiry) + " hrs");
            CookieResponse cookieResponse = mongoRepoService.getCookieResponse(username);
            UserProfile userProfile = mongoRepoService.getUserProfile(username);
            erpData.setCookies(cookieResponse);
            erpData.setUserProfile(userProfile);
            isLoginCookiesSecured = true;
        } else {
            initiateLogin();
        }
    }

    public void initiateLogin() throws IOException, InterruptedException {
        saveEmptyDataToMongo();
        resetReadAttempt();
        getSessionCookies();
        getCaptcha();

        if (readCaptcha())
            getLoginCookies();
        else
            System.out.println("Failed to Read Captcha");

        if (isLoginCookiesSecured) {
            getRoleId();
            getRoleCookies();
            getDashboardCookies();
        }
    }

    private void saveEmptyDataToMongo() {
        mongoRepoService.getCookieRepo().save(erpData.getCookies());
        if (mongoRepoService.getUserProfileRepo().findById(username).isEmpty())
            mongoRepoService.getUserProfileRepo().save(erpData.getUserProfile());
    }

    public void getDashboardCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(erpData.getUserProfile().getStudentDataRefererUrl()))
                .headers(erpData.getHeaders(erpData.getCookiesForDashBoard(), erpData.getUserProfile().getChooseUserRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ArrayList<Cookie> dashboardCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), erpData.getDashboardKeys());
        erpData.getCookies().setDashboardCookiesData(dashboardCookiesData);

        mongoRepoService.updateDashboardCookiesData(username, dashboardCookiesData);

        System.out.println("End of Get-Dashboard-Cookies\n");
    }

    public void getRoleCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + CHOOSE_ROLE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpData.getBodyForRole()))
                .headers(erpData.getHeaders(erpData.getCookiesForChooseRole(), erpData.getUserProfile().getChooseUserRefererUrl()))
                .build();
        System.out.println(erpData.getBodyForRole());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("role-cookies-status: " + response.statusCode());

        ArrayList<Cookie> roleCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), erpData.getRoleKeys());
        erpData.getCookies().setRoleCookiesData(roleCookiesData);

        String dashboardUrl = saveDashboardUrl(response.body());

        mongoRepoService.updateRoleCookiesData(username, roleCookiesData);
        mongoRepoService.updateStudentDataRefererUrl(username, dashboardUrl);

        System.out.println("End of Get-Role-Cookies\n");
    }

    public void getRoleId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ROLE_ID_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpData.getHeaders(erpData.getCookiesForChooseRole(), erpData.getUserProfile().getChooseUserRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("role-id-status: " + response.statusCode());
        String roleId = saveRoleId(response.body());

        mongoRepoService.updateRoleId(username, roleId);

        System.out.println("End of Get-Role-Id\n");
    }

    public void getLoginCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + LOGIN_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpData.getBodyForLogin(username, pass)))
                .headers(erpData.getHeaders(erpData.getCookiesForLogin(), HOST_URL + MAIN_URL))
                .build();
        System.out.println(erpData.getBodyForLogin(username, pass));

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("login-cookies-status: " + response.statusCode());

        if (isCaptchaInvalid(response.body()))
            return;
        if (isCredentialsInvalid(response.body()))
            return;

        ArrayList<Cookie> loginCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), erpData.getLoginKeys());
        erpData.getCookies().setLoginCookiesData(loginCookiesData);

        String roleUrl = saveChooseRoleRefererUrl(response.body());
        isLoginCookiesSecured = true;

        mongoRepoService.updateLoginCookiesData(username, loginCookiesData);
        mongoRepoService.updateChooseUserRefererUrl(username, roleUrl);

        System.out.println("End of Get-Login-Cookies\n");
    }

    public boolean readCaptcha() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(flaskServerUrl + "read-captcha"))
                .POST(HttpRequest.BodyPublishers.ofString(erpData.getBodyForReadCaptcha()))
                .header("content-type", "application/json")
                .build();

        System.out.println("Read-Captcha-Body: " + erpData.getBodyForReadCaptcha());
        System.out.println(flaskServerUrl);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("read-captcha-status: " + response.statusCode());

        String captchaTxt = erpData.checkCaptcha(response.body());
        System.out.println(response.body());

        System.out.println("Captcha Reading Attempt: " + captchaReadAttempt);
        System.out.println(captchaTxt);

        if (captchaTxt.length() == 6) {
            erpData.setCaptchaTxt(captchaTxt);
            System.out.println("Read-Captcha ended successfully.\n");
            if (captchaReadAttempt > 0)
                getLoginCookies();
            else
                return true;
        } else {
            handleFailedCaptcha();
            return false;
        }
        return false;
    }

    public void getCaptcha() throws IOException, InterruptedException {
        String url = HOST_URL + CAPTCHA_URL + Math.random() + Math.random();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("cookie", erpData.getCookiesForCaptcha())
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("get-captcha-status: " + response.statusCode());
        String imageDecodedDataString = Base64.getEncoder().encodeToString(response.body());

        for (Map.Entry<String, List<String>> e : response.headers().map().entrySet()) {
            for (String s : e.getValue()) {
                if (e.getKey().contains("set-cookie") && s.contains(erpData.getCaptchaKey())) {
                    int end = s.indexOf(";");
                    String cookie = s.substring(0, end);
                    String expiry = erpDataUtils.extractExpiry(s, end);

                    System.out.println("New Captcha: " + cookie);
                    Cookie captchaData = new Cookie(cookie, expiry);
                    erpData.setCaptchaImgByteData(imageDecodedDataString);
                    erpData.getCookies().setCaptchaData(captchaData);

                    mongoRepoService.updateCaptcha(username, captchaData);
                    break;
                }
            }
        }

        System.out.println("End Of Get-Captcha\n");
    }

    public void getSessionCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + MAIN_URL))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("session-cookies-status: " + response.statusCode());

        ArrayList<Cookie> extractedData = erpDataUtils.extractCookiesMap(response.headers().map(), erpData.getSessionKeys());
        erpData.getCookies().setSessionCookiesData(extractedData);

        mongoRepoService.updateSessionCookiesData(username, extractedData);

        System.out.println("End of Get-Session-Cookies\n");
    }

    public void resetReadAttempt() {
        captchaReadAttempt = 0;
    }
//      --------------------------END OF LOGIN BLOCK--------------------------------

    //    ----------------------------SAVE DATA FUNCTIONS---------------------------------
    private String saveRoleId(String body) {
        Map<String, Object> result = (Map<String, Object>) ((ArrayList) erpDataUtils.getResponseMap(body)).get(0);

        if (result.containsKey("roleId_P")) {
            String roleId = result.get("roleId_P").toString();
            erpData.getUserProfile().setRoleId(roleId);
            return roleId;
        }
        return null;
    }

    private String saveDashboardUrl(String body) {
        String result = (String) erpDataUtils.getResponseMap(body);
        String url = HOST_URL + result;
        erpData.getUserProfile().setStudentDataRefererUrl(url);
        return url;
    }

    private String saveChooseRoleRefererUrl(String body) {
        Map<String, Object> result = (Map<String, Object>) erpDataUtils.getResponseMap(body);

        if (result.containsKey("NavigateUrl")) {
            String url = HOST_URL + result.get("NavigateUrl");
            erpData.getUserProfile().setChooseUserRefererUrl(url);
            return url;
        }
        return null;
    }

    //      ---------------------------CHECK FUNCTIONS---------------------
    private boolean isCredentialsInvalid(String body) {
        Map<String, Object> result = (Map<String, Object>) erpDataUtils.getResponseMap(body);
        String msg = getLoginResponseMessage(result);

        return msg.contains("invalid user name");
    }

    private boolean isCaptchaInvalid(String body) throws IOException, InterruptedException {
        Map<String, Object> result = (Map<String, Object>) erpDataUtils.getResponseMap(body);
        String msg = getLoginResponseMessage(result);

        if (msg.contains("invalid captcha")) {
            System.out.println("Captcha Failed at Login!");
            System.out.println(msg);
            handleFailedCaptcha();
            return true;
        }
        return false;
    }

    private String getLoginResponseMessage(Map<String, Object> result) {
        String msg = "";
        if (result.containsKey("Message")) {
            Object ob = result.get("Message");
            msg = ob != null ? ob.toString().toLowerCase() : "";
        }
        return msg;
    }

    //      -------------------------------HANDLE CAPTCHA-----------------------------
    private void inputCaptcha() {
        System.out.println("View captcha at location: ./assets/captcha-img.png and enter text below::");
        Scanner sc = new Scanner(System.in);
        String captchaTxt = sc.next().trim();
        erpData.setCaptchaTxt(captchaTxt);
    }

    private void handleFailedCaptcha() throws IOException, InterruptedException {
        System.out.println("Captcha Failed Again!\n");

        if (captchaReadAttempt == CAPTCHA_RETRY_LIMIT) {
            System.out.println("Stopping Algorithm...\n");
            throw new InterruptedException("Retry Limit Reached");
        }

        captchaReadAttempt++;
        getCaptcha();
        readCaptcha();
    }


    //      ------------------------------GET SET FUNCTIONS----------------------
    public boolean isLoginCookiesSecured() {
        return isLoginCookiesSecured;
    }

    public ErpData getErpData() {
        return erpData;
    }

    //    --------------------------------GET RESPONSES TO API-------------------

    public void destroy() {
//        instance = null;
    }
}
