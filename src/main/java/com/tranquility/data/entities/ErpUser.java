package com.tranquility.data.entities;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@RequiredArgsConstructor
@Document(collection = "erpUser")
public class ErpUser {
    @Id
    @NonNull
    private String id;
    private String roleId;
    private String chooseUserRefererUrl;
    private String studentDataRefererUrl;
}
