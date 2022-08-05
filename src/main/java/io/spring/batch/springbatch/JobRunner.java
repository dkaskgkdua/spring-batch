package io.spring.batch.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobRunner
        implements ApplicationRunner
{
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
//                .addLong("seq", 4L)
//                .addDouble("age", 16.5)
                .addString("requestDate", "20220805")
                .toJobParameters();
        jobLauncher.run(job, jobParameters);
    }
}
