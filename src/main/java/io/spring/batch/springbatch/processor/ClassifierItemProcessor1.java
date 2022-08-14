package io.spring.batch.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;

public class ClassifierItemProcessor1 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {
    @Override
    public ProcessorInfo process(ProcessorInfo processorInfo) throws Exception {
        System.out.println("ClassifierItemProcessor1 exc");
        return processorInfo;
    }
}
