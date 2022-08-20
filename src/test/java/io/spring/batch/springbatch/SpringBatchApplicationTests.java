package io.spring.batch.springbatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes={DBJobConfiguration.class, TestBatchConfig.class})
class SpringBatchApplicationTests {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void simpleJob_test() throws Exception{
        //given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("user", "user1")
                .addLong("date", new Date().getTime())
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        JobExecution step1 = jobLauncherTestUtils.launchStep("step1");

        //then
        Assert.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
        Assert.assertEquals(jobExecution.getExitStatus(), ExitStatus.COMPLETED);

        StepExecution stepExecution = (StepExecution) ((List) step1.getStepExecutions()).get(0);

        Assert.assertEquals(stepExecution.getCommitCount(), 10);
        Assert.assertEquals(stepExecution.getReadCount(), 1000);
        Assert.assertEquals(stepExecution.getWriteCount(), 1000);
    }

    @After
    public void clear() {
        jdbcTemplate.execute("delete from customer2");
    }

}
