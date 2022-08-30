package io.spring.batch.springbatch.example1.rowmapper;

import io.spring.batch.springbatch.example1.domain.ProductVo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<ProductVo> {
    @Override
    public ProductVo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ProductVo.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getInt("price"))
                .type(rs.getString("type"))
                .build()
                ;
    }
}
