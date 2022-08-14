package io.spring.batch.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;

public class ClassifierItemProcessor3 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {
    @Override
    public ProcessorInfo process(ProcessorInfo processorInfo) throws Exception {
        System.out.println("ClassifierItemProcessor3 exc");
        return processorInfo;
    }
}
