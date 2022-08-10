package io.spring.batch.springbatch.reader.mapper;

import io.spring.batch.springbatch.dto.Customer2;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer2> {
    @Override
    public Customer2 mapFieldSet(FieldSet fieldSet) throws BindException {
        if(fieldSet == null) return null;

        Customer2 customer = new Customer2();
        customer.setName(fieldSet.readString(0));
        customer.setAge(fieldSet.readInt(1));
        customer.setYear(fieldSet.readString(2));
        return customer;
    }
}
