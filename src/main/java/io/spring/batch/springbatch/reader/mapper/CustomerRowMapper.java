package io.spring.batch.springbatch.reader.mapper;

import io.spring.batch.springbatch.dto.Customer3;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRowMapper implements RowMapper<Customer3> {
    @Override
    public Customer3 mapRow(ResultSet rs, int i) throws SQLException {
        return new Customer3(rs.getLong("id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getDate("birthdate"));
    }
}
