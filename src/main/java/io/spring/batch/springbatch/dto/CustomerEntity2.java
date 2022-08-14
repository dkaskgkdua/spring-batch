package io.spring.batch.springbatch.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "customer2")
public class CustomerEntity2 {
    @Id
    @GeneratedValue
    private Long id;
    private String firstname;
    private String lastname;
    private Date birthdate;

    @OneToOne(mappedBy = "customer")
    private Address address;
}
