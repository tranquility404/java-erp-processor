package com.tranquility.data;

import com.tranquility.model.Cookie;
import com.tranquility.model.CookieResponse;
import com.tranquility.model.UserData;
import com.tranquility.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MongoRepoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CookieRepo cookieRepo;

    @Autowired
    private UserProfileRepo userProfileRepo;

    @Autowired
    private UserDataRepo userDataRepo;

    public CookieRepo getCookieRepo() {
        return cookieRepo;
    }

    public UserProfileRepo getUserProfileRepo() {
        return userProfileRepo;
    }

    public UserDataRepo getUserDataRepo() {
        return userDataRepo;
    }

    public CookieResponse getCookieResponse(String id) {
        Optional<CookieResponse> optional = cookieRepo.findById(id);
        return optional.orElse(null);
    }

    public UserProfile getUserProfile(String id) {
        Optional<UserProfile> optional = userProfileRepo.findById(id);
        return optional.orElse(null);
    }

    public String getFirstLoginCookieExpiry(String id) {
//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.match(new Criteria("id").is(id)),
//                Aggregation.unwind("loginCookiesData"),
//                Aggregation.project("loginCookiesData.expiry")
//                        .and("loginCookiesData.expiry").as("expiry"),
//                Aggregation.limit(1)
//        );

        Query query = new Query(Criteria.where("id").is(id));
        query.fields().include("loginCookiesData");
        CookieResponse cookieResponse = mongoTemplate.findOne(query, CookieResponse.class);
        if (cookieResponse != null && cookieResponse.getLoginCookiesData() != null && !cookieResponse.getLoginCookiesData().isEmpty())
            return cookieResponse.getLoginCookiesData().get(0).getExpiry();

        return null;

//        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "cookieResponse", Map.class);

//        System.out.println(Arrays.toString(results.getMappedResults().toArray()));
//        if (!results.getMappedResults().isEmpty()) {
//            Map<String, Object> result = results.getMappedResults().get(0);
//            return (String) result.get("expiry");
//        }

//        return null;
    }

    public void updateSessionCookiesData(String id, List<Cookie> newSessionCookiesData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("sessionCookiesData", newSessionCookiesData);
        mongoTemplate.updateFirst(query, update, CookieResponse.class);
    }

    public void updateCaptcha(String id, Cookie captchaData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("captchaData", captchaData);
        mongoTemplate.updateFirst(query, update, CookieResponse.class);
    }

    public void updateLoginCookiesData(String id, List<Cookie> newLoginCookiesData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("loginCookiesData", newLoginCookiesData);
        mongoTemplate.updateFirst(query, update, CookieResponse.class);
    }

    public void updateRoleCookiesData(String id, List<Cookie> newRoleCookiesData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("roleCookiesData", newRoleCookiesData);
        mongoTemplate.updateFirst(query, update, CookieResponse.class);
    }

    public void updateDashboardCookiesData(String id, List<Cookie> newDashboardCookiesData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("dashboardCookiesData", newDashboardCookiesData);
        mongoTemplate.updateFirst(query, update, CookieResponse.class);
    }


    public void updateRoleId(String id, String roleId) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("roleId", roleId);
        mongoTemplate.updateFirst(query, update, UserProfile.class);
    }

    public void updateChooseUserRefererUrl(String id, String chooseUserRefererUrl) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("chooseUserRefererUrl", chooseUserRefererUrl);
        mongoTemplate.updateFirst(query, update, UserProfile.class);
    }

    public void updateStudentDataRefererUrl(String id, String studentDataRefererUrl) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("studentDataRefererUrl", studentDataRefererUrl);
        mongoTemplate.updateFirst(query, update, UserProfile.class);
    }

    public void updateStudentData(String id, String studentData) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("studentData", studentData);
        mongoTemplate.updateFirst(query, update, UserData.class);
    }
}
