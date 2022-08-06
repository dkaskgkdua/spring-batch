package io.spring.batch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.FlowStepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.job.JobParametersExtractor;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public Job detailFlowJob() {
        return jobBuilderFactory.get("detailFlowJob")
                .start(step1())
                .on("COMPLETED").to(step3())
                .from(step1())
                .on("FAILED").to(step2())
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
                    stepContribution.setExitStatus(ExitStatus.STOPPED);
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


}
