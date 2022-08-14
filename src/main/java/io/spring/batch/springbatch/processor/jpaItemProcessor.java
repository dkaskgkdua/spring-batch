package io.spring.batch.springbatch.processor;

import io.spring.batch.springbatch.Customer;
import io.spring.batch.springbatch.dto.CustomerEntity;
import io.spring.batch.springbatch.dto.CustomerEntity2;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class jpaItemProcessor implements ItemProcessor<CustomerEntity, CustomerEntity2> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public CustomerEntity2 process(CustomerEntity customer) throws Exception {
        System.out.println("process exc");
        CustomerEntity2 customerEntity2 = modelMapper.map(customer, CustomerEntity2.class);
        return customerEntity2;
    }
}
