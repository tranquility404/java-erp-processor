package com.tranquility.data.repositories;

import com.tranquility.data.entities.ErpData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpDataRepo extends MongoRepository<ErpData, String> {
    @Query(value = "{ 'id': ?0 }", fields = "{ 'studentData' : 1, '_id' : 0 }")
    String findStudentDataById(String id);

    @Query(value = "{ 'id': ?0 }", fields = "{ 'attendance' : 1, '_id' : 0 }")
    String findAttendanceById(String id);
}
