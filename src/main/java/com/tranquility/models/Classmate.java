package com.tranquility.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Classmate {
    private String name;
    private String admissionCode;
    private String email;
}
