package io.spring.batch.springbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Customer3 {
    private Long id;
    private String firstname;
    private String lastname;
    private Date birthdate;
}
