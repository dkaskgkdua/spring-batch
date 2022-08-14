package io.spring.batch.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;

public class ClassifierItemProcessor2 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {
    @Override
    public ProcessorInfo process(ProcessorInfo processorInfo) throws Exception {
        System.out.println("ClassifierItemProcessor2 exc");
        return processorInfo;
    }
}
