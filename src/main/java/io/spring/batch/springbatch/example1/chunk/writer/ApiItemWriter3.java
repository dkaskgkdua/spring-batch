package io.spring.batch.springbatch.example1.chunk.writer;

import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ApiResponseVo;
import io.spring.batch.springbatch.example1.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class ApiItemWriter3 implements ItemWriter<ApiRequestVo> {
    private final AbstractApiService apiService;


    @Override
    public void write(List<? extends ApiRequestVo> items) throws Exception {
        ApiResponseVo responseVo = apiService.service(items);
        System.out.println("responseVo = " + responseVo);

    }
}
