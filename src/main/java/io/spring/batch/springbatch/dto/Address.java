package io.spring.batch.springbatch.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Address {
    @Id
    @GeneratedValue
    private Long id;
    private String location;

    @OneToOne
    @JoinColumn(name = "id")
    private CustomerEntity customer;
}
