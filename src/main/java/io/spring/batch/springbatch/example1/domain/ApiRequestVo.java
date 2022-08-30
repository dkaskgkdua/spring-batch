package io.spring.batch.springbatch.example1.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiRequestVo {
    private long id;
    private ProductVo productVo;

}
