package com.tranquility.data.entities;

import com.tranquility.models.Cookie;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@RequiredArgsConstructor
@Document(collection = "erpCookieResponse")
public class CookieResponse {
    @Id
    @NonNull
    private String id;
    private List<Cookie> sessionCookiesData;
    private Cookie captchaData;
    private List<Cookie> loginCookiesData;
    private List<Cookie> roleCookiesData;
    private List<Cookie> dashboardCookiesData;
}
