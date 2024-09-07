package com.tranquility.data.repositories;

import com.tranquility.data.entities.CookieResponse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CookieRepo extends MongoRepository<CookieResponse, String> {}