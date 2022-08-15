package io.spring.batch.springbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer3 {
    private Long id;
    private String firstname;
    private String lastname;
    private Date birthdate;
}
