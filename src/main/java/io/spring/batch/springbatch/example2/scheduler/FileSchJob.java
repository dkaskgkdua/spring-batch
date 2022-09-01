package io.spring.batch.springbatch.example2.scheduler;

import lombok.SneakyThrows;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileSchJob extends QuartzJobBean {
    @Qualifier("example2FileJob")
    @Autowired
    private Job example2FileJob;

    @Autowired
    private JobLauncher jobLauncher;

    // read의 기능만 있음
    @Autowired
    private JobExplorer jobExplorer;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        String requestDate = (String) context.getJobDetail().getJobDataMap().get("requestDate");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("id", new Date().getTime())
                .addString("requestDate", requestDate)
                .toJobParameters();

        // job 이름을 기준으로 여러개를 다 가져옴
        int jobInstanceCount = jobExplorer.getJobInstanceCount(example2FileJob.getName());
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(example2FileJob.getName(), 0, jobInstanceCount);

        if(jobInstances.size() > 0) {
            for(JobInstance jobInstance : jobInstances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);

                // 똑같은 날짜가 있는 리스트들을 필터링함
                List<JobExecution> jobExecutionList = jobExecutions.stream().filter(jobExecution ->
                        jobExecution.getJobParameters().getString("requestDate").equals(requestDate)).collect(Collectors.toList());
                if(jobExecutionList.size() > 0) {
                    throw new JobExecutionException(requestDate + " already exists");
                }
            }
        }

        jobLauncher.run(example2FileJob, jobParameters);
    }
}
