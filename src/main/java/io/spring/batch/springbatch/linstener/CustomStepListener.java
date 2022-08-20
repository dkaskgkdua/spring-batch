package io.spring.batch.springbatch.linstener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class CustomStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putString("name2", "mj");
        String stepName = stepExecution.getStepName();
        System.out.println("stepName = " + stepName);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus exitStatus = stepExecution.getExitStatus();
        System.out.println("exitStatus = " + exitStatus);

        BatchStatus status = stepExecution.getStatus();
        System.out.println("status = " + status);

        String name2 = (String)stepExecution.getExecutionContext().get("name2");
        System.out.println("name2 = " + name2);
        return ExitStatus.COMPLETED;
    }
}
