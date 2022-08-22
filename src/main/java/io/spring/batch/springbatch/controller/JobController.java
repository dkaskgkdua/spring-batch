package io.spring.batch.springbatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;

    @PostMapping(value = "batch/start")
    public String start(@RequestBody JobInfo jobInfo) throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {

        for(Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext();) {
            SimpleJob job = (SimpleJob)jobRegistry.getJob(iterator.next());
            System.out.println("job = " + job.getName());
            jobOperator.start(job.getName(), "id="+jobInfo.getId());
        }

        return "batch is started";
    }

    @PostMapping(value = "batch/stop")
    public String stop() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException, NoSuchJobExecutionException, JobExecutionNotRunningException {

        for(Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext();) {
            SimpleJob job = (SimpleJob)jobRegistry.getJob(iterator.next());
            System.out.println("job = " + job.getName());

            // 현재 잡이 실행중인지...
            Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(job.getName());
            JobExecution jobExecution = runningJobExecutions.iterator().next();

            // 잡 종료 (현재 실행중인 잡까지는 실행하고 종료함)
            jobOperator.stop(jobExecution.getId());
        }

        return "batch is ended";
    }


    @PostMapping(value = "batch/restart")
    public String restart() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException, NoSuchJobExecutionException, JobExecutionNotRunningException, JobInstanceAlreadyCompleteException, JobRestartException {

        for(Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext();) {
            SimpleJob job = (SimpleJob)jobRegistry.getJob(iterator.next());
            System.out.println("job = " + job.getName());

            // 현재 잡이 실행중인지...
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(job.getName());
            JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);


            // 잡 종료 (현재 실행중인 잡까지는 실행하고 종료함)
            jobOperator.restart(lastJobExecution.getId());
        }

        return "batch is restarted";
    }
}
