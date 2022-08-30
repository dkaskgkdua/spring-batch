package io.spring.batch.springbatch.example1.job.api;

import io.spring.batch.springbatch.classifier.ProcessorClassifier;
import io.spring.batch.springbatch.example1.chunk.processor.ApiItemProcessor1;
import io.spring.batch.springbatch.example1.chunk.processor.ApiItemProcessor2;
import io.spring.batch.springbatch.example1.chunk.processor.ApiItemProcessor3;
import io.spring.batch.springbatch.example1.chunk.writer.ApiItemWriter1;
import io.spring.batch.springbatch.example1.chunk.writer.ApiItemWriter2;
import io.spring.batch.springbatch.example1.chunk.writer.ApiItemWriter3;
import io.spring.batch.springbatch.example1.classifier.ApiProcessorClassifier;
import io.spring.batch.springbatch.example1.classifier.ApiWriterClassifier;
import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import io.spring.batch.springbatch.example1.partition.ProductPartitioner;
import io.spring.batch.springbatch.example1.service.ApiService1;
import io.spring.batch.springbatch.example1.service.ApiService2;
import io.spring.batch.springbatch.example1.service.ApiService3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ApiStepConfiguration {
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final ApiService1 apiService1;
    private final ApiService2 apiService2;
    private final ApiService3 apiService3;
    private int chunkSize = 10;

    @Bean
    public Step apiMasterStep() throws Exception {
        return stepBuilderFactory.get("apiMasterStep")
                .partitioner(apiSlaveStep().getName(), partitioner())
                .step(apiSlaveStep())
                .gridSize(3)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(6);
        taskExecutor.setThreadNamePrefix("api-thread-");

        return taskExecutor;
    }

    @Bean
    public Step apiSlaveStep() throws Exception {
        return stepBuilderFactory.get("apiSlaveStep")
                .<ProductVo, ProductVo>chunk(chunkSize)
                .reader(itemReader(null))
                .processor(apiSlaveItemProcessor())
                .writer(apiSlaveItemWriter())
                .build();
    }

    @Bean
    public ItemWriter apiSlaveItemWriter() {
        ClassifierCompositeItemWriter<ApiRequestVo > writer
                = new ClassifierCompositeItemWriter<>();
        ApiWriterClassifier<ApiRequestVo, ItemWriter<? super ApiRequestVo>> classifier = new ApiWriterClassifier<>();
        Map<String, ItemWriter<ApiRequestVo>> writerMap = new HashMap<>();
        writerMap.put("1", new ApiItemWriter1(apiService1));
        writerMap.put("2", new ApiItemWriter2());
        writerMap.put("3", new ApiItemWriter3());

        classifier.setProcessorMap(writerMap);
        writer.setClassifier(classifier);
        return writer;
    }

    @Bean
    public ItemProcessor apiSlaveItemProcessor() {
        ClassifierCompositeItemProcessor<ProductVo, ApiRequestVo > processor
                = new ClassifierCompositeItemProcessor<>();
        ApiProcessorClassifier<ProductVo, ItemProcessor<?, ? extends ApiRequestVo>> classifier = new ApiProcessorClassifier<>();
        Map<String, ItemProcessor<ProductVo, ApiRequestVo>> processorMap = new HashMap<>();
        processorMap.put("1", new ApiItemProcessor1());
        processorMap.put("2", new ApiItemProcessor2());
        processorMap.put("3", new ApiItemProcessor3());

        classifier.setProcessorMap(processorMap);
        processor.setClassifier(classifier);
        return processor;
    }

    @Bean
    public ProductPartitioner partitioner() {
        ProductPartitioner productPartitioner = new ProductPartitioner();
        productPartitioner.setDataSource(dataSource);
        return productPartitioner;
    }

    @Bean
    @StepScope
    public ItemReader<ProductVo> itemReader(@Value("#{stepExecutionContext['product']}") ProductVo productVo) throws Exception {
        JdbcPagingItemReader<ProductVo> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setPageSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(ProductVo.class));

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, name, price, type");
        queryProvider.setFromClause("from product");
        queryProvider.setWhereClause("where type = :type");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.DESCENDING);
        queryProvider.setSortKeys(sortKeys);

        reader.setParameterValues(QueryGenerator.getParameterForQuery("type", productVo.getType()));
        reader.setQueryProvider(queryProvider);
        reader.afterPropertiesSet();

        return reader;
    }
}
