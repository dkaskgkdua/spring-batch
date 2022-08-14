package io.spring.batch.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;

public class CompositeProcessor2 implements ItemProcessor<String, String> {
    int cnt = 0;

    @Override
    public String process(String s) throws Exception {
        cnt++;
        return s + cnt;
    }
}
