package com.tranquility.model


data class ErpData (
    var cookies: CookieResponse,
    var userProfile: UserProfile,

    var captchaImgByteData: String? = null,
    var captchaTxt: String? = null,
) {

    val sessionKeys: Array<String> = arrayOf("ASP.NET_SessionId=", "astro=", "astro0=")
    val captchaKey: String = "astro14="
    val loginKeys: Array<String> = arrayOf(
        "astro38=", "astro39=", "astro19=", "astro34=",
        "astro33=", "astro13=", "astro11=", "astro9=",
        "astro10=", "astro3=", "astro1=", "astro2=",
        "astro4=", "astro12=", "astro35=", "astro36=",
        "astro37="
    )
    val roleKeys: Array<String> = arrayOf("astro5=", "astro6=", "astro7=", "astro8=")
    val dashboardKeys: Array<String> = arrayOf("astro20=", "astro26=", "astro27=")

    companion object {
        fun getInstance(id: String): ErpData = ErpData(CookieResponse(id), UserProfile(id))
    }

    fun getCookiesForCaptcha(): String = cookies.sessionCookiesData!!.joinToString(separator = "; ", transform = { it.cookie })

    fun getCookiesForLogin(): String = "${getCookiesForCaptcha()}; ${cookies.captchaData?.cookie}"

    fun checkCaptcha(txt: String): String {
        val newTxt = txt.trim().filter { c -> !c.isWhitespace() }

        return if (newTxt.length > 6)
            newTxt.take(6)
        else
            newTxt
    }

    fun getBodyForReadCaptcha() = "{\"captcha-img-byte-data\":\"${captchaImgByteData}\"}"

    fun getBodyForLogin(username: String, pass: String): String = "{\"userId\":\"$username\",\"passwds\":\"$pass\",\"vcaptcha\":\"$captchaTxt\",\"openFor\":\"Students\",\"institute\":\"ab0d4edc-07f0-45de-aac9-9917b3ea16ef\"}"

    fun getHeaders(cookies: String, referer: String, accept: String): Array<String> = arrayOf(
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
    )

    fun getHeaders(cookies: String, referer: String): Array<String> = getHeaders(cookies, referer, "application/json, text/javascript, */*; q=0.01")

    fun getCookiesForChooseRole(): String = "${getCookiesForLogin()}; ${cookies.loginCookiesData!!.joinToString(separator = "; ", transform = { it.cookie })}"

    fun getBodyForRole(): String = "{\"rId\":\"${userProfile.roleId}\"}"

    fun getCookiesForDashBoard(): String = "${getCookiesForChooseRole()}; ${cookies.roleCookiesData!!.joinToString(separator = "; ", transform = { it.cookie })}"

    fun getBodyForAttendance(studentData: Map<String, Any>): String = "{ \"sid\": \"${studentData["StudentId_P"]}\",\"subject\": \"0\",\"month\": \"0\",\"prg\": \"${studentData["prgId_P"]}\",\"branch\": \"${studentData["branchId_P"]}\",\"ys\": \"${studentData["stuSem_P"]}\" }"

    fun getCookiesForSubjects(): String = "${getCookiesForDashBoard()}; ${cookies.dashboardCookiesData!!.joinToString(separator = "; ", transform = { it.cookie })}"

//    Try this for employees data once you get employees cookies:
    fun getBodyForCirculars(month: Int, year: Int): String = "{\"title\":\"\",\"month\":\"$month\",\"year\":\"$year\",\"category\":\"0\",\"cFor\":\"S\"}"

}
