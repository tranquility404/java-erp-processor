package com.tranquility.data.entities;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@RequiredArgsConstructor
@Document(collection = "erpData")
public class ErpData {
    @Id
    @NonNull
    private String id;
    private String studentData;
    private String attendance;
}