package com.tranquility.data;

import com.tranquility.model.CookieResponse;
import com.tranquility.model.UserData
import com.tranquility.model.UserProfile
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CookieRepo : MongoRepository<CookieResponse, String>

@Repository
interface UserProfileRepo: MongoRepository<UserProfile, String>

@Repository
interface UserDataRepo: MongoRepository<UserData, String> {
    @Query(value = "{ 'id': ?0 }", fields = "{ 'studentData' : 1, '_id' : 0 }")
    fun findStudentDataById(id: String?): String?

    @Query(value = "{ 'id': ?0 }", fields = "{ 'attendance' : 1, '_id' : 0 }")
    fun findAttendanceById(id: String?): String?
}
