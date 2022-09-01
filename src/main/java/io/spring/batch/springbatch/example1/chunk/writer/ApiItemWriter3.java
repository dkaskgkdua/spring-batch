package io.spring.batch.springbatch.example1.chunk.writer;

import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ApiResponseVo;
import io.spring.batch.springbatch.example1.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@RequiredArgsConstructor
public class ApiItemWriter3 extends FlatFileItemWriter<ApiRequestVo> {
    private final AbstractApiService apiService;


    @Override
    public void write(List<? extends ApiRequestVo> items) throws Exception {
        ApiResponseVo responseVo = apiService.service(items);
        System.out.println("responseVo = " + responseVo);

        items.forEach(item -> item.setApiResponseVo(responseVo));

        super.setResource(new FileSystemResource("D:\\Users\\dkask\\IdeaProjects\\spring-batch\\src\\main\\resources\\txt\\product3.txt"));
        super.open(new ExecutionContext());
        super.setLineAggregator(new DelimitedLineAggregator<>());
        super.setAppendAllowed(true);
        super.write(items);
    }
}
