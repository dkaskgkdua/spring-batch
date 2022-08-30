package io.spring.batch.springbatch.example1.chunk.processor;

import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import org.springframework.batch.item.ItemProcessor;

public class ApiItemProcessor3 implements ItemProcessor<ProductVo, ApiRequestVo> {
    @Override
    public ApiRequestVo process(ProductVo item) throws Exception {
        return ApiRequestVo.builder()
                .id(item.getId())
                .productVo(item)
                .build();

    }
}
