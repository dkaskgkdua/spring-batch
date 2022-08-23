package io.spring.batch.springbatch.example1.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVo {
    private Long id;
    private String name;
    private int price;
    private String type;
}
