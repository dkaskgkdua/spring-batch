package io.spring.batch.springbatch;

import io.spring.batch.springbatch.decider.CustomDecider;
import io.spring.batch.springbatch.dto.Customer2;
import io.spring.batch.springbatch.linstener.CustomStepListener;
import io.spring.batch.springbatch.linstener.JobListener;
import io.spring.batch.springbatch.linstener.JobRepositoryListener;
import io.spring.batch.springbatch.linstener.PassCheckingListener;
import io.spring.batch.springbatch.processor.CustomItemProcessor;
import io.spring.batch.springbatch.reader.CustomItemReader;
import io.spring.batch.springbatch.reader.CustomItemStreamReader;
import io.spring.batch.springbatch.reader.mapper.CustomerFieldSetMapper;
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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
                .reader(platFilesItemReader())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List list) throws Exception {
                        System.out.println("list = " + list);
                    }
                })
                .build();
    }

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
