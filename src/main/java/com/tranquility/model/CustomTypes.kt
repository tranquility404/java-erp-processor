package com.tranquility.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cookieResponse")
data class CookieResponse(
    @Id
    val id: String,
    var sessionCookiesData: ArrayList<Cookie>? = null,
    var captchaData: Cookie? = null,
    var loginCookiesData: ArrayList<Cookie>? = null,
    var roleCookiesData: ArrayList<Cookie>? = null,
    var dashboardCookiesData: ArrayList<Cookie>? = null
)

data class Cookie(
    var cookie: String,
    var expiry: String?
)

//data class CaptchaData(
//    var cookie: String,
//    var expiry: String,
//)

@Document(collection = "userProfile")
data class UserProfile(
    @Id
    val id: String,
    var roleId: String? = null,
    var chooseUserRefererUrl: String? = null,
    var studentDataRefererUrl: String? = null
)

@Document(collection = "userJsonData")
data class UserData(
    @Id
    val id: String,
    var studentData: String? = null,
    var attendance: String? = null
) {
    companion object {
        fun instance(id: String): UserData = UserData(id)
    }
}

data class Subject(
    var name: String,
    var type: String,
    var syllabusLink: String
)

data class Classmate(
    var name: String,
    var admissionCode: String,
    var email: String
)

data class Circular(
    var title: String,
    var category: String,
    var url: String,
    var date: String
)

