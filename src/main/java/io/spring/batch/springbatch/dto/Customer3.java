package io.spring.batch.springbatch.dto;

import lombok.Data;

import java.util.Date;

@Data
public class Customer3 {
    private Long id;
    private String firstName;
    private String lastName;
    private Date birthdate;
}
