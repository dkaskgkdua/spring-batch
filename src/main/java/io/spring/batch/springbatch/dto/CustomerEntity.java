package io.spring.batch.springbatch.dto;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "customer")
public class CustomerEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String firstname;
    private String lastname;
    private Date birthdate;

    @OneToOne(mappedBy = "customer")
    private Address address;
}
