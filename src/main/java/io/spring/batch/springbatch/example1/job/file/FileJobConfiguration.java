package io.spring.batch.springbatch.example1.job.file;

import io.spring.batch.springbatch.example1.chunk.processor.FileItemProcessor;
import io.spring.batch.springbatch.example1.domain.Product;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FileJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManager transactionManager;

    @Bean
    public PlatformTransactionManager jpaTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job exampleFileJob() {
        return jobBuilderFactory.get("exampleFileJob")
                .start(exampleFileStep())
                .build();
    }

    @Bean
    public Step exampleFileStep() {
        return stepBuilderFactory.get("exampleFileStep")
                .<ProductVo, Product>chunk(10)
                .reader(fileItemReader(null))
                .processor(fileItemProcessor())
                .writer(fileItemWriter())
                .transactionManager(jpaTransactionManager())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<ProductVo> fileItemReader(@Value("#{jobParameters['requestDate']}") String requestDate) {
        return new FlatFileItemReaderBuilder<ProductVo>()
                .name("flatFile")
                .resource(new ClassPathResource("/csv/product_" + requestDate + ".csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(ProductVo.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("id", "name", "price", "type")
                .build();

    }

    @Bean
    public ItemProcessor<ProductVo, Product> fileItemProcessor() {
        return new FileItemProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<Product> fileItemWriter() {
        return new JpaItemWriterBuilder<Product>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

}
