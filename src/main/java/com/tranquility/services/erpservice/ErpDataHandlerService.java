package com.tranquility.services.erpservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranquility.data.entities.CookieResponse;
import com.tranquility.data.entities.ErpData;
import com.tranquility.data.entities.ErpUser;
import com.tranquility.data.entities.User;
import com.tranquility.models.*;
import com.tranquility.services.MongoRepoService;
import com.tranquility.services.userservice.UserService;
import com.tranquility.utils.ScrapeHelper;
import com.tranquility.utils.Utils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
//@Slf4j
public class ErpDataHandlerService {

    private final int CAPTCHA_RETRY_LIMIT = 10;
    private int captchaReadAttempt = 0;
    private boolean isLoginCookiesSecured = false;

    private User user;

    private final String HOST_URL = "https://erp.axiscolleges.net/";

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

    @Getter
    private ErpScrapedData erpScrapedData;
    private ErpDataUtils erpDataUtils;
    private final HttpClient client = HttpClient.newHttpClient();
//    private DataDumpHandler dataDumpHandler;

    @Autowired
    private MongoRepoService mongoRepoService;
    @Autowired
    private UserService userService;

    @Autowired
    public ErpDataHandlerService(@Value("${flask.server.url}") String flaskServerUrl/*, @Value("${spring.data.mongodb.uri}") String mongo*/) {
        this.flaskServerUrl = flaskServerUrl;
//        System.out.println("Flask-Server: " + flaskServerUrl);
//        System.out.println("Mongo-Db: " + mongo);
//            this.dataDumpHandler = new DataDumpHandler(user.getUsername(), pass);
    }

    public void initialize(User user) {
        this.user = user;
        erpScrapedData = new ErpScrapedData(user.getUsername());
        erpDataUtils = new ErpDataUtils(erpScrapedData);
    }

    public List<Map<String, String>> getAcademicCalendar() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ACADEMIC_CALENDAR_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{\"calendarId\":\"5\"}"))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForSubjects(), erpScrapedData.getErpUser().getStudentDataRefererUrl()))
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
                .POST(HttpRequest.BodyPublishers.ofString(erpScrapedData.getBodyForCirculars(date[0], date[1])))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForSubjects(), erpScrapedData.getErpUser().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("End of Get Circulars");
        return ScrapeHelper.extractCirculars(response.body());
    }

    public List<Classmate> getClassmates() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + CLASSMATES_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForSubjects(), erpScrapedData.getErpUser().getStudentDataRefererUrl()))
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
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForSubjects(), erpScrapedData.getErpUser().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        String html = mapper.readTree(response.body()).path("d").asText();

        System.out.println("End of Get Subjects");
        return ScrapeHelper.extractSubjects(html);
    }

    public String getAttendance() throws IOException, InterruptedException {
        Map<String, String> studentData = erpDataUtils.convertToHashMap(mongoRepoService.getErpDataRepo().findStudentDataById(user.getUsername()));
        System.out.println(erpScrapedData.getBodyForAttendance(studentData));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ATTENDANCE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpScrapedData.getBodyForAttendance(studentData)))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForDashBoard(), erpScrapedData.getErpUser().getStudentDataRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String attendanceData = (String) erpDataUtils.getResponseMap(response.body());

        System.out.println("End of Get Attendance");
        return attendanceData;
    }

    public Map<String, Object> getStudentData() throws IOException, InterruptedException {
        mongoRepoService.getErpDataRepo().save(new ErpData(user.getUsername()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + STUDENT_DATA_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForDashBoard(), erpScrapedData.getErpUser().getChooseUserRefererUrl(), "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("student-data-status: " + response.statusCode());

        System.out.println(response.body());
        ArrayList list = (ArrayList) erpDataUtils.getResponseMap(response.body());

        if (!list.isEmpty()) {
            Map<String, Object> studentData = (Map<String, Object>) list.get(0);
            String jsonString = new ObjectMapper().writeValueAsString(studentData);
            saveName(jsonString);
            mongoRepoService.updateStudentData(user.getUsername(), jsonString);
            return studentData;
        }

        System.out.println("End of Get-Student-Data");
        return null;
    }

    private void saveName(String jsonString) {
        try {
            Map<String, String> map = new ObjectMapper().readValue(jsonString, new TypeReference<>() {});
            String name = map.get("studentFullName_P");
            mongoRepoService.updateStudentName(user.getUsername(), name);
        } catch (Exception e) {
            System.out.println("Error occurred while extracting Student Name: ");
            System.out.println(e.toString());
        }
    }


    //      -----------------LOGIN FUNCTIONS-------------------------------------------
    public void loginIfExpired() throws IOException, InterruptedException {
        String expiry = mongoRepoService.getFirstLoginCookieExpiry(user.getUsername());

        if (expiry != null && Utils.calculateRemainingTime(expiry) > 1) {
            System.out.println("Expiry: " + Utils.calculateRemainingTime(expiry) + " hrs");
            CookieResponse cookieResponse = mongoRepoService.getCookieResponse(user.getUsername());
            ErpUser erpUser = mongoRepoService.getErpUser(user.getUsername());
            erpScrapedData.setCookies(cookieResponse);
            erpScrapedData.setErpUser(erpUser);
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
        mongoRepoService.getCookieRepo().save(erpScrapedData.getCookies());
        if (mongoRepoService.getErpUserRepo().findById(user.getUsername()).isEmpty())
            mongoRepoService.getErpUserRepo().save(erpScrapedData.getErpUser());
    }

    public void getDashboardCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(erpScrapedData.getErpUser().getStudentDataRefererUrl()))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForDashBoard(), erpScrapedData.getErpUser().getChooseUserRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Cookie> dashboardCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), ErpScrapedData.DataKeys.dashboardKeys);
        erpScrapedData.getCookies().setDashboardCookiesData(dashboardCookiesData);

        mongoRepoService.updateDashboardCookiesData(user.getUsername(), dashboardCookiesData);

        System.out.println("End of Get-Dashboard-Cookies\n");
    }

    public void getRoleCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + CHOOSE_ROLE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpScrapedData.getBodyForRole()))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForChooseRole(), erpScrapedData.getErpUser().getChooseUserRefererUrl()))
                .build();
        System.out.println(erpScrapedData.getBodyForRole());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("role-cookies-status: " + response.statusCode());

        List<Cookie> roleCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), ErpScrapedData.DataKeys.roleKeys);
        erpScrapedData.getCookies().setRoleCookiesData(roleCookiesData);

        String dashboardUrl = saveDashboardUrl(response.body());

        mongoRepoService.updateRoleCookiesData(user.getUsername(), roleCookiesData);
        mongoRepoService.updateStudentDataRefererUrl(user.getUsername(), dashboardUrl);

        System.out.println("End of Get-Role-Cookies\n");
    }

    public void getRoleId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + ROLE_ID_URL))
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForChooseRole(), erpScrapedData.getErpUser().getChooseUserRefererUrl()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("role-id-status: " + response.statusCode());
        String roleId = saveRoleId(response.body());

        mongoRepoService.updateRoleId(user.getUsername(), roleId);

        System.out.println("End of Get-Role-Id\n");
    }

    public void getLoginCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + LOGIN_URL))
                .POST(HttpRequest.BodyPublishers.ofString(erpScrapedData.getBodyForLogin(user.getUsername(), user.getPassword(), user.getInstitute())))
                .headers(erpScrapedData.getHeaders(erpScrapedData.getCookiesForLogin(), HOST_URL + erpScrapedData.getLoginPageUrl(user.getInstitute())))
                .build();
        System.out.println(erpScrapedData.getBodyForLogin(user.getUsername(), user.getPassword(), user.getInstitute()));

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("login-cookies-status: " + response.statusCode());

        if (isCaptchaInvalid(response.body()))
            return;
        if (isCredentialsInvalid(response.body()))
            return;

        List<Cookie> loginCookiesData = erpDataUtils.extractCookiesMap(response.headers().map(), ErpScrapedData.DataKeys.loginKeys);
        erpScrapedData.getCookies().setLoginCookiesData(loginCookiesData);

        String roleUrl = saveChooseRoleRefererUrl(response.body());
        isLoginCookiesSecured = true;

        mongoRepoService.updateLoginCookiesData(user.getUsername(), loginCookiesData);
        mongoRepoService.updateChooseUserRefererUrl(user.getUsername(), roleUrl);

        System.out.println("End of Get-Login-Cookies\n");
    }

    public boolean readCaptcha() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(flaskServerUrl + "/read-captcha"))
                .POST(HttpRequest.BodyPublishers.ofString(erpScrapedData.getBodyForReadCaptcha()))
                .header("content-type", "application/json")
                .build();

        System.out.println("Read-Captcha-Body: " + erpScrapedData.getBodyForReadCaptcha());
        System.out.println(flaskServerUrl);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("read-captcha-status: " + response.statusCode());

        String captchaTxt = erpScrapedData.checkCaptcha(response.body());
        System.out.println(response.body());

        System.out.println("Captcha Reading Attempt: " + captchaReadAttempt);
        System.out.println(captchaTxt);

        if (captchaTxt.length() == 6) {
            erpScrapedData.setCaptchaTxt(captchaTxt);
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
                .header("cookie", erpScrapedData.getCookiesForCaptcha())
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("get-captcha-status: " + response.statusCode());
        String imageDecodedDataString = Base64.getEncoder().encodeToString(response.body());

        for (Map.Entry<String, List<String>> e : response.headers().map().entrySet()) {
            for (String s : e.getValue()) {
                if (e.getKey().contains("set-cookie") && s.contains(ErpScrapedData.DataKeys.captchaKey)) {
                    int end = s.indexOf(";");
                    String cookie = s.substring(0, end);
                    String expiry = erpDataUtils.extractExpiry(s, end);

                    System.out.println("New Captcha: " + cookie);
                    Cookie captchaData = new Cookie(cookie, expiry);
                    erpScrapedData.setCaptchaImgByteData(imageDecodedDataString);
                    erpScrapedData.getCookies().setCaptchaData(captchaData);

                    mongoRepoService.updateCaptcha(user.getUsername(), captchaData);
                    break;
                }
            }
        }

        System.out.println("End Of Get-Captcha\n");
    }

    public void getSessionCookies() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOST_URL + erpScrapedData.getLoginPageUrl(user.getInstitute())))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("session-cookies-status: " + response.statusCode());

        List<Cookie> extractedData = erpDataUtils.extractCookiesMap(response.headers().map(), ErpScrapedData.DataKeys.sessionKeys);
        erpScrapedData.getCookies().setSessionCookiesData(extractedData);

        mongoRepoService.updateSessionCookiesData(user.getUsername(), extractedData);

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
            erpScrapedData.getErpUser().setRoleId(roleId);
            return roleId;
        }
        return null;
    }

    private String saveDashboardUrl(String body) {
        String result = (String) erpDataUtils.getResponseMap(body);
        String url = HOST_URL + result;
        erpScrapedData.getErpUser().setStudentDataRefererUrl(url);
        return url;
    }

    private String saveChooseRoleRefererUrl(String body) {
        Map<String, Object> result = (Map<String, Object>) erpDataUtils.getResponseMap(body);

        if (result.containsKey("NavigateUrl")) {
            String url = HOST_URL + result.get("NavigateUrl");
            erpScrapedData.getErpUser().setChooseUserRefererUrl(url);
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
        erpScrapedData.setCaptchaTxt(captchaTxt);
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

    //    --------------------------------GET RESPONSES TO API-------------------

    public void destroy() {
//        instance = null;
    }
}
