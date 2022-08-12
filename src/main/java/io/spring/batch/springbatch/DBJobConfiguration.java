package io.spring.batch.springbatch;

import io.spring.batch.springbatch.decider.CustomDecider;
import io.spring.batch.springbatch.dto.Customer2;
import io.spring.batch.springbatch.dto.Customer3;
import io.spring.batch.springbatch.dto.Customer4;
import io.spring.batch.springbatch.dto.CustomerEntity;
import io.spring.batch.springbatch.linstener.CustomStepListener;
import io.spring.batch.springbatch.linstener.JobListener;
import io.spring.batch.springbatch.linstener.JobRepositoryListener;
import io.spring.batch.springbatch.linstener.PassCheckingListener;
import io.spring.batch.springbatch.processor.CustomItemProcessor;
import io.spring.batch.springbatch.reader.CustomItemReader;
import io.spring.batch.springbatch.reader.CustomItemStreamReader;
import io.spring.batch.springbatch.reader.mapper.CustomerFieldSetMapper;
import io.spring.batch.springbatch.service.CustomService;
import io.spring.batch.springbatch.writer.CustomItemStreamWriter;
import io.spring.batch.springbatch.writer.CustomItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class DBJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomTasklet1 customTasklet1;
    private final CustomTasklet2 customTasklet2;
    private final CustomTasklet3 customTasklet3;
    private final CustomTasklet4 customTasklet4;
    private final JobRepositoryListener jobRepositoryListener;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    /**
     * ItemReaderAdapter를 활용한 다른 Service, Dao 사용
     */
    @Bean
    public Job readerAdapterJob() {
        return jobBuilderFactory.get("readerAdapterJob")
                .start(readerAdapterStep())
                .build();
    }

    @Bean
    public Step readerAdapterStep() {
        return stepBuilderFactory.get("readerAdapterStep")
                .<String, String>chunk(10)
                .reader(readerAdapterItemReader())
                .writer(readerAdapterItemWriter())
                .build();
    }

    @Bean
    public ItemReader<String> readerAdapterItemReader() {
        ItemReaderAdapter<String> reader = new ItemReaderAdapter<>();
        reader.setTargetObject(customService());
        reader.setTargetMethod("customRead");
        return reader;
    }

    @Bean
    public Object customService() {
        return new CustomService<String>();
    }

    @Bean
    public ItemWriter<String> readerAdapterItemWriter() {
        return items -> {
            System.out.println("items = " + items);
        };
    }

    /**
     * jpa 기반 paging 배치 처리
     */
    @Bean
    public Job jpaPagingJob() throws Exception{
        return jobBuilderFactory.get("jpaPagingJob")
                .start(jpaPagingStep())
                .build();
    }

    @Bean
    public Step jpaPagingStep() throws Exception {
        return stepBuilderFactory.get("jpaPagingStep")
                .<CustomerEntity, CustomerEntity>chunk(10)
                .reader(jpaPagingItemReader())
                .writer(jpaCursorItemWriter())
                .build();
    }

    @Bean
    public ItemReader<? extends CustomerEntity> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<CustomerEntity>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("select c from customer c join fetch c.address")
                .build();
    }


    /**
     * jdbc 기반 paging 배치 처리
     * 여러 쓰레드에서 작업할 경우 synchronized가 걸려 있어 동시성 문제를 방지
     */
    @Bean
    public Job jdbcPagingJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcPagingStep())
                .build();
    }

    @Bean
    public Step jdbcPagingStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingStep")
                .<Customer3,Customer3>chunk(3)
                .reader(jdbcPagingItemReader())
                .writer(jdbcPagingItemWriter())
                .build();
    }

    @Bean
    public ItemReader<? extends Customer3> jdbcPagingItemReader() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "mj%");

        return new JdbcPagingItemReaderBuilder<Customer3>()
                .name("jdbcPagingItemReader")
                .pageSize(10)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Customer3.class))
                .queryProvider(createQueryProvider())
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {


        SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
        factory.setDataSource(dataSource);
        factory.setSelectClause("id,firstname,lastname,birthdate");
        factory.setFromClause("from customer");
        factory.setWhereClause("where firstname like :firstname");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        factory.setSortKeys(sortKeys);

        return factory.getObject();
    }

    @Bean
    public ItemWriter<Customer3> jdbcPagingItemWriter() {
        return items -> {
            for(Customer3 item : items) {
                System.out.println("items = " + item);
            }
        };
    }


    /**
     * jpa 기반 cursor 배치 처리
     */
    @Bean
    public Job jpaCursorJob() {
        return jobBuilderFactory.get("jpaCursorJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaCursorStep())
                .build();
    }

    @Bean
    public Step jpaCursorStep() {
        return stepBuilderFactory.get("jpaCursorStep")
                .<CustomerEntity,CustomerEntity>chunk(3)
                .reader(jpaCursorItemReader())
                .writer(jpaCursorItemWriter())
                .build();
    }

    @Bean
    public ItemReader<CustomerEntity> jpaCursorItemReader() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "mj%");

        return new JpaCursorItemReaderBuilder<CustomerEntity>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from CustomerEntity c where firstname like :firstname")
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public ItemWriter<CustomerEntity> jpaCursorItemWriter() {
        return items -> {
            for(CustomerEntity item : items) {
                System.out.println("items = " + item);
            }
        };
    }

    /**
     * jdbc 기반 cursor 배치처리
     */

    @Bean
    public Job jdbcCursorJob() {
        return jobBuilderFactory.get("jdbcCursorJob")
                .start(jdbcCursorStep())
                .build();
    }

    @Bean
    public Step jdbcCursorStep() {
        return stepBuilderFactory.get("jdbcCursorStep")
                .<Customer3, Customer3>chunk(10)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer3> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Customer3>()
                .name("jdbcCursorItemReader")
                .fetchSize(10)
                .sql("select id, firstName, lastName, birthdate from customer where firstName like ? order by id")
                .beanRowMapper(Customer3.class)
                .queryArguments("mj%")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<Customer3> jdbcCursorItemWriter() {
        System.out.println("---writer---");
        return items -> {
            for(Customer3 item : items) {
                System.out.println("item = " + item);
            }
        };
    }

    @Bean
    public Job jsonFileJob() {
        return jobBuilderFactory.get("jsonFileJob")
                .start(jsonFileStep())
                .next(step2())
                .build();
    }

    @Bean
    public Step jsonFileStep() {
        return stepBuilderFactory.get("jsonFileStep")
                .<Customer4, Customer4>chunk(5)
                .reader(jsonFileItemReader())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List list) throws Exception {
                        System.out.println("list = " + list);
                    }
                })
                .build();
    }

    @Bean
    public ItemReader<Customer4> jsonFileItemReader() {
        return new JsonItemReaderBuilder<Customer4>()
                .name("jsonFileItemReader")
                .resource(new ClassPathResource("/json/customer.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(Customer4.class))
                .build();
    }

    @Bean
    public Job xmlFileJob() {
        return jobBuilderFactory.get("xmlFileJob")
                .start(xmlFileStep())
                .next(step2())
                .build();
    }
    @Bean
    public Step xmlFileStep() {
        return stepBuilderFactory.get("xmlFileStep")
                .<Customer3, Customer3>chunk(5)
                .reader(xmlFileItemReader())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List list) throws Exception {
                        System.out.println("list = " + list);
                    }
                })
                .build();
    }


    @Bean
    public StaxEventItemReader<Customer3> xmlFileItemReader() {
        return new StaxEventItemReaderBuilder<Customer3>()
                .name("xmlFileItemReader")
                .resource(new ClassPathResource("/xml/customer.xml"))
                .addFragmentRootElements("customer")
                .unmarshaller(itemMarshaller())
                .build();
    }

    @Bean
    public XStreamMarshaller itemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("customer", Customer3.class);
        aliases.put("id", Long.class);
        aliases.put("firstName", String.class);
        aliases.put("lastName", String.class);
        aliases.put("birthdate", Date.class);
        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);
        return xStreamMarshaller;
    }

    @Bean
    public Job flatFilesJob() {
        return jobBuilderFactory.get("flatFilesJob")
                .start(flatFilesStep())
                .next(step2())
                .build();
    }

    @Bean
    public Step flatFilesStep() {
        return stepBuilderFactory.get("flatFilesStep")
                .<String, String>chunk(5)
                .reader(flatFileItemFixedTokenReader())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List list) throws Exception {
                        System.out.println("list = " + list);
                    }
                })
                .build();
    }

    /**
     * 기본적으로 제공해주는 Builder를 안쓰고 직접 만들때
     */
    @Bean
    public ItemReader platFilesItemReader() {
        FlatFileItemReader<Customer2> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("/csv/customer.csv"));

        DefaultLineMapper<Customer2> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(new CustomerFieldSetMapper());

        itemReader.setLineMapper(lineMapper);
        // 첫 라인은 건너뛴다.(타이틀)
        itemReader.setLinesToSkip(1);

        return itemReader;
    }

    /**
     * 고정길이만큼 잘라서 읽고 반환해주는 리더
     */
    @Bean
    public FlatFileItemReader flatFileItemFixedTokenReader() {
        return new FlatFileItemReaderBuilder<Customer2>()
                .name("flatFilesFactoryItemReader")
                .resource(new ClassPathResource("/csv/customer.txt"))
//                .fieldSetMapper(new CustomerFieldSetMapper()) // 커스텀 매퍼 사용시
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                // BeanWrapperFieldSetMapper 가 무슨 타입으로 매핑할 것인지 세팅
                .targetType(Customer2.class)
                .linesToSkip(1) // 첫 라인은 건너뛴다.(타이틀)
                // 구분자 방식으로 한다.
                .fixedLength()
                // 파싱 에러에 엄격하게 적용할것인지
                .strict(false)
                .addColumns(new Range(1,5))
                .addColumns(new Range(6,9))
                .addColumns(new Range(10,11))
                // 매핑할 필드명
                .names("name", "year", "age")
                .build()
                ;
    }

    /**
     * 리더를 빌더를 통해 생성할때, 구분자로 데이터 긁어옴
     */
    @Bean
    public ItemReader flatFilesFactoryItemReader() {
        return new FlatFileItemReaderBuilder<Customer2>()
                .name("flatFilesFactoryItemReader")
                .resource(new ClassPathResource("/csv/customer.csv"))
//                .fieldSetMapper(new CustomerFieldSetMapper()) // 커스텀 매퍼 사용시
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                // BeanWrapperFieldSetMapper 가 무슨 타입으로 매핑할 것인지 세팅
                .targetType(Customer2.class)
                .linesToSkip(1) // 첫 라인은 건너뛴다.(타이틀)
                // 구분자 방식으로 한다.
                .delimited()
                // 콤마로 구분
                .delimiter(",")
                // 매핑할 필드명
                .names("name", "age", "year")
                .build()
                ;
    }

    @Bean
    public Job chunkStreamJob() {
        return jobBuilderFactory.get("batchJob")
                .start(chunkStreamStep1())
                .next(step2())
                .build();
    }

    @Bean
    public Step chunkStreamStep1() {
        return stepBuilderFactory.get("chunkStreamStep1")
                .<String, String>chunk(5)
                .reader(itemStreamReader())
                .writer(itemStreamWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super String> itemStreamWriter() {
        return new CustomItemStreamWriter();
    }

    public CustomItemStreamReader itemStreamReader() {
        List<String> items = new ArrayList<>(10);

        for(int i = 0; i < 10; i++) {
            items.add(String.valueOf(i));
        }
        return new CustomItemStreamReader(items);
    }

    @Bean
    public Job chunkBaseJob() {
        return jobBuilderFactory.get("chunkBaseJob")
                .start(chunkStep1())
                .next(step7())
                .build();
    }

    @Bean
    public Step chunkStep1() {
        return stepBuilderFactory.get("chunkStep1")
                .<Customer, Customer>chunk(3)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer> itemWriter() {
        return new CustomItemWriter();
    }

    @Bean
    public ItemProcessor<? super Customer, ? extends Customer> itemProcessor() {
        return new CustomItemProcessor();
    }

    @Bean
    public ItemReader<Customer> itemReader() {
        return new CustomItemReader(Arrays.asList(new Customer("user1"),
                new Customer("user2"),new Customer("user3")));
    }


    @Bean
    public Job scopeJob() {
        return jobBuilderFactory.get("scopeJob")
                .start(scopeStep1(null))
                .next(scopeStep2())
                .listener(new JobListener())
                .build();
    }

    @Bean
    public Job deciderJob() {
        return jobBuilderFactory.get("deciderJob")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(decider())
                .from(decider()).on("ODD").to(step7())
                .from(decider()).on("EVEN").to(step8())
                .end()
                .build();
    }

    // 사용자 정의 ExitStatus 예제
    @Bean
    public Job customCodeJob() {
        return jobBuilderFactory.get("customCodeJob")
                .start(failedStep())
                    .on("FAILED")
                    .to(applyCustomListenerStep())
                // 만약 PASS를 제외한 다른 상태코드가 온다면 최종 job은 실패로 끝난다
                // 이유는 지정해준 코드값이 없기 때문에
                    .on("PASS")
                    .stop()
                .end()
                .build();
    }

    // 상속관계 형태의 Job
    // --job.name=parentJob
    @Bean
    public Job parentJob() {
        return jobBuilderFactory.get("parentJob")
                .start(jobStep())
                .next(step9())
                .build();
    }

    // --job.name=caseJob
    @Bean
    public Job caseJob() {
        return jobBuilderFactory.get("caseJob")
                .start(step1())
                .next(chunkStep())
//                .next(partitionerStep())
                .next(errorStep())
//                .incrementer(new CustomJobParametersIncrementer())
                .build();
    }

//    --job.name=job name=user2 requestDate=20220805
    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .next(step3())
                .next(step4())
                .next(step5())
                .next(step6())
                .listener(jobRepositoryListener)
                .build();
    }

    /**
     * on : ExitStatus와 매칭하는 패턴 스키마
     *      특수문자는
     *       * : 모든 ExitStatus와 매칭 / 0개 이상)
     *       ? : 정확히 1개의 문자와 매칭
     *       ex) c*t 는 cat 또는 count와 같은 케이스에 매칭
     *           c?t 는 cat에만 count는 안됨
     */
    @Bean
    public Job detailFlowJob() {
        return jobBuilderFactory.get("detailFlowJob")
                .start(step1())
                .on(ExitStatus.COMPLETED.getExitCode()).to(step3())
                .from(step1())
                .on("FAILED").to(step2())
                .end()
                .build();
    }

    /**
     * 총 3개의 Flow
     *
     */
    @Bean
    public Job transitionFlowJob() {
        return jobBuilderFactory.get("transitionFlowJob")
                .start(step1())
                    .on("FAILED")
                    .to(step2())
                    .on("FAILED")
                    .stop()
                .from(step1())
                    .on("*")
                    .to(step3())
                    .next(step4())
                // 1 Flow에서 step2실행이 FAILED가 아니면 이쪽으로 온다
                .from(step2())
                    .on("*")
                    .to(step5())
                .end()
                .build();
    }

    // --job.name=flowJob
    @Bean
    public Job flowJob() {
        return jobBuilderFactory.get("flowJob")
                .start(flow())
                .next(step9())
                .end()
                .build();
    }

    @Bean
    public Job flowJob2() {
        return jobBuilderFactory.get("flowJob2")
                .start(flowA())
                .next(step1())
                .next(flowB())
                .next(step2())
                .end()
                .build();
    }

    private Flow flowB() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowA");
        flowBuilder.start(step6())
                .next(step7())
                .end();
        return flowBuilder.build();
    }

    private Flow flowA() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowA");
        flowBuilder.start(step8())
                .next(step9())
                .end();
        return flowBuilder.build();
    }

    //--job.name=configJob
    @Bean
    public Job SampleConfigJob() {
        return jobBuilderFactory.get("configJob")
                .start(step1())
                .next(step2())
                .next(errorStep())
                .incrementer(new CustomJobParametersIncrementer())
                // 기본 제공 인크리먼터
//                .incrementer(new RunIdIncrementer())
                // 기본 벨리데이트에 옵션 추가(필수값, 옵션값)
                .validator(new DefaultJobParametersValidator(new String[]{"name", "requestDate", "run.id"}, new String[]{"count"}))
//              익명클래스 형태 커스텀 벨리데이트
//                .validator(new JobParametersValidator() {
//                    @Override
//                    public void validate(JobParameters jobParameters) throws JobParametersInvalidException {
//
//                    }
//                })
                // 커스텀 벨리데이트
//                .validator(new CustomJobParametersValidator())
                .preventRestart()
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {

                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {

                    }
                })
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step1 !");
                        // 타입에 맞게 받는방식
                        JobParameters jobParameters = stepContribution.getStepExecution().getJobExecution().getJobParameters();
                        jobParameters.getString("name");
                        jobParameters.getLong("seq");
                        jobParameters.getDate("date");
                        jobParameters.getDouble("age");

                        // map으로 파라미터 받는 방식
                        Map<String, Object> jobParameters1 = chunkContext.getStepContext().getJobParameters();


                        return RepeatStatus.FINISHED;
                    }
                })
                // 성공종료 해도 무조건 실행
                .allowStartIfComplete(true)
                .build();

    }
    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step2 !");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();

    }
    @Bean
    public Step step3() {
        return stepBuilderFactory.get("custom step1")
                .tasklet(customTasklet1)
                .build();

    }
    @Bean
    public Step step4() {
        return stepBuilderFactory.get("custom step2")
                .tasklet(customTasklet2)
                .build();

    }
    @Bean
    public Step step5() {
        return stepBuilderFactory.get("custom step3")
                .tasklet(customTasklet3)
                .build();

    }
    @Bean
    public Step step6() {
        return stepBuilderFactory.get("custom step4")
                .tasklet(customTasklet4)
                .build();

    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(step7())
                .next(step8())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step7() {
        return stepBuilderFactory.get("step7")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step7 !");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
    @Bean
    public Step step8() {
        return stepBuilderFactory.get("step8")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step8 !");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
    @Bean
    public Step step9() {
        return stepBuilderFactory.get("step9")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step9 !");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step failedStep() {
        return stepBuilderFactory.get("failedStep")
                .tasklet((stepContribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().setStatus(BatchStatus.FAILED);
                    stepContribution.setExitStatus(ExitStatus.FAILED);
                    System.out.println("failedStep has executed!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step errorStep() {
        return stepBuilderFactory.get("errorStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("errorStep execution");
                    throw new RuntimeException("errorStep was failed");
//                    return RepeatStatus.FINISHED;
                })
                .startLimit(3)
                .build();
    }

    @Bean
    public Step chunkStep() {
        return stepBuilderFactory.get("chunkStep")
                .<String, String>chunk(10)
//                .reader(new ItemReader<String>() {
//                    @Override
//                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//                        System.out.println("chunk read!");
//                        return null;
//                    }
//                })
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2","item3", "item4","item5", "item6")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {

                        return item.toUpperCase();
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> items) throws Exception {
                        items.forEach(item -> System.out.println("item = " + item));
                        System.out.println("chunk write!");
                    }
                })
                .build();
    }

    // 멀티스레드 작업을 위한 파티셔너 스텝
    @Bean
    public Step partitionerStep() {
        return stepBuilderFactory.get("partitionerStep")
                .partitioner(step1())
                .gridSize(2)
                .build();
    }

    @Bean
    public Step jobStep() {
        return stepBuilderFactory.get("jobStep")
                .job(jobForStep())
//                .launcher(jobLauncher)
                // Step의 ExecutionContext를 job이 실행되는데 필요한 JobParameter로 변경
                .parametersExtractor(jobParametersExtractor())
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        //jobParametersExtractor에서 name 키값을 받아오기 위한 세팅
                        stepExecution.getExecutionContext().put("name", "user1");
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return null;
                    }
                })
                .build();
    }

    private DefaultJobParametersExtractor jobParametersExtractor() {
        DefaultJobParametersExtractor extractor = new DefaultJobParametersExtractor();
        extractor.setKeys(new String[]{"name"});
        return extractor;
    }

    @Bean
    public Step flowStep() {
        return stepBuilderFactory.get("flowStep")
                .flow(flow())
                .build();
    }

    @Bean
    public Job jobForStep() {
        return this.jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .next(step3())
                .build();
    }

    @Bean
    public Step applyCustomListenerStep() {
        return stepBuilderFactory.get("applyCustomListenerStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("applyCustomListenerStep executed");
                    return RepeatStatus.FINISHED;
                })
                .listener(new PassCheckingListener())
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new CustomDecider();
    }

    @Bean
    @JobScope
    public Step scopeStep1(@Value("#{jobParameters['message']}") String message) {
        System.out.println("message = " + message);
        return stepBuilderFactory.get("scopeStep1")
                .tasklet(scopeTasklet(null))
                .build();
    }
    @Bean
    @JobScope
    public Step scopeStep2() {
        return stepBuilderFactory.get("scopeStep2")
                .tasklet(scopeTasklet2(null))
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet scopeTasklet(@Value("#{jobExecutionContext['name']}") String name) {
        System.out.println("name = " + name);
        return (stepContribution, chunkContext) -> {
            System.out.println("scopeTasklet1 executed!");
            return RepeatStatus.FINISHED;
        };
    }
    @Bean
    @StepScope
    public Tasklet scopeTasklet2(@Value("#{stepExecutionContext['name2']}") String name2) {
        System.out.println("name2 = " + name2);
        return (stepContribution, chunkContext) -> {
            System.out.println("scopeTasklet2 executed!");
            return RepeatStatus.FINISHED;
        };
    }


}
