package io.spring.batch.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
