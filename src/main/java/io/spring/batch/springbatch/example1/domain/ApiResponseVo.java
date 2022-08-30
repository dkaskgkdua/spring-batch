package io.spring.batch.springbatch.example1.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponseVo {
    private int status;
    private String msg;
}
