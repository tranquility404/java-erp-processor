package com.tranquility.data.repositories;

import com.tranquility.data.entities.ErpUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpUserRepo extends MongoRepository<ErpUser, String> {}
