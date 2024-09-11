package com.tranquility.models;

import com.tranquility.data.entities.CookieResponse;
import com.tranquility.data.entities.ErpUser;
import lombok.Data;
import org.bson.json.JsonObject;

import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ErpScrapedData {

    CookieResponse cookies;
    ErpUser erpUser;

    String captchaImgByteData;
    String captchaTxt;

    public ErpScrapedData(String username) {
        cookies = new CookieResponse(username);
        erpUser = new ErpUser(username);
    }

    public static class DataKeys {
        public static String[] sessionKeys = new String[]{"ASP.NET_SessionId=", "astro=", "astro0="};
        public static String captchaKey = "astro14=";
        public static String[] loginKeys = new String[]{
                "astro38=", "astro39=", "astro19=", "astro34=",
                "astro33=", "astro13=", "astro11=", "astro9=",
                "astro10=", "astro3=", "astro1=", "astro2=",
                "astro4=", "astro12=", "astro35=", "astro36=",
                "astro37="
        };
        public static String[] roleKeys = new String[]{"astro5=", "astro6=", "astro7=", "astro8="};
        public static String[] dashboardKeys = new String[]{"astro20=", "astro26=", "astro27="};
    }

    private static class Institutes {
        public static final String AxisInstituteOfTechnologyAndManagement = "ab0d4edc-07f0-45de-aac9-9917b3ea16ef";
        public static final String AxisInstituteOfFashionAndTechnology = "55520610-2a9f-4d9a-9b7f-8427b7e6402e";
        public static final String AxisInstituteOfArchitecture = "59b3b886-f773-452b-aef1-33abcb1e8515";
        public static final String AxisBusinessSchool = "2ffa293e-75b3-4520-887f-3fd02d0cd676";
        public static final String AxisInstituteOfPlanningAndManagement = "b2e19fe8-1bb5-4a02-8953-9e0e42ae850f";
        public static final String AxisInstituteOfHigherEducation = "56c3f8c3-bc12-4390-9a32-25a416021935";
        public static final String AxisInstituteOfPharmacy = "046c523f-a8a6-4a3b-9a19-0dc7de8808bf";
        public static final String AxisInstituteOfDiplomaEngineering = "75a7451f-34b4-4ec4-b88c-20d49b746d6a";
    }

    public String getLoginPageUrl(int institute) {
        return "index.aspx?openFor=Students&institute=" + getInstitute(institute);
    }

    private String getInstitute(int institute) {
        return switch (institute) {
            case 1 -> Institutes.AxisInstituteOfTechnologyAndManagement;
            case 2 -> Institutes.AxisInstituteOfFashionAndTechnology;
            case 3 -> Institutes.AxisInstituteOfArchitecture;
            case 4 -> Institutes.AxisBusinessSchool;
            case 5 -> Institutes.AxisInstituteOfPlanningAndManagement;
            case 6 -> Institutes.AxisInstituteOfHigherEducation;
            case 7 -> Institutes.AxisInstituteOfPharmacy;
            case 8 -> Institutes.AxisInstituteOfDiplomaEngineering;
            default -> null;
        };
    }

    public String getCookiesForCaptcha() {
        return cookies.getSessionCookiesData().stream().map(Cookie::getCookie).collect(Collectors.joining("; "));
    }

    public String getCookiesForLogin() {
        return getCookiesForCaptcha() + "; " + cookies.getCaptchaData().getCookie();
    }

    public String checkCaptcha(String txt) {
        String newTxt = txt.replace(" ", "");
        return newTxt.length() > 6 ? newTxt.substring(0, 6) : newTxt;
    }

    public String getBodyForReadCaptcha() {
        return "{\"captcha-img-byte-data\":\"" + captchaImgByteData + "\"}";
    }

    public String getBodyForLogin(String username, String password, int institute) {
        return "{\"userId\":\"" + username +
                "\",\"passwds\":\"" + password +
                "\",\"vcaptcha\":\"" + captchaTxt +
                "\",\"openFor\":\"Students\",\"institute\":\"" + getInstitute(institute) + "\"}";
    }

    public String[] getHeaders(String cookies, String referer, String accept) {
        return new String[]{
                "cookie", cookies,
                "content-type", "application/json; charset=UTF-8",
                "accept", accept,
                "accept-language", "en-US",
                "accept-encoding", "gzip, deflate, br",
                "origin", "https://erp.axiscolleges.net",
                "sec-fetch-site", "same-origin",
                "sec-fetch-mode", "cors",
                "sec-fetch-dest", "empty",
                "referer", referer
        };
    }

    public String[] getHeaders(String cookies, String referer) {
        return getHeaders(cookies, referer, "application/json, text/javascript, */*; q=0.01");
    }

    public String getCookiesForChooseRole() {
        return getCookiesForLogin() + "; " + cookies.getLoginCookiesData().stream().map(Cookie::getCookie).collect(Collectors.joining("; "));
    }

    public String getBodyForRole() {
        return "{\"rId\":\"" + erpUser.getRoleId() + "\"}";
    }

    public String getCookiesForDashBoard() {
        return getCookiesForChooseRole() + "; " + cookies.getRoleCookiesData().stream().map(Cookie::getCookie).collect(Collectors.joining("; "));
    }

    public String getBodyForAttendance(Map<String, String> studentData) {
        return "{ \"sid\": \"" + studentData.get("StudentId_P") +
                "\",\"subject\": \"0\"," +
                "\"month\": \"0\"," +
                "\"prg\": \"" + studentData.get("prgId_P") +
                "\",\"branch\": \"" + studentData.get("branchId_P") +
                "\",\"ys\": \"" + studentData.get("stuSem_P") + "\" }";
    }

    public String getCookiesForSubjects() {
        return getCookiesForDashBoard() + "; " + cookies.getDashboardCookiesData().stream().map(Cookie::getCookie).collect(Collectors.joining("; "));
    }

    public String getBodyForCirculars(int month, int year) {       //    Try this for employees data once you get employees cookies:
        return "{\"title\":\"\"," +
                "\"month\":\"" + month +
                "\",\"year\":\"" + year +
                "\",\"category\":\"0\"," +
                "\"cFor\":\"S\"}";
    }

}
