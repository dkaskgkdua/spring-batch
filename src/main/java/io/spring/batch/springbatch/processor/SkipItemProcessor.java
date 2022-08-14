package io.spring.batch.springbatch.processor;

import io.spring.batch.springbatch.exception.SkippableException;
import org.springframework.batch.item.ItemProcessor;

public class SkipItemProcessor implements ItemProcessor<String, String> {
    private int cnt = 0;
    @Override
    public String process(String item) throws Exception {
        if(item.equals("6") || item.equals("7")) {
            System.out.println("ItemProcess exception: " + item);
            throw new SkippableException("Process failed cnt : " + cnt);
        } else {
            System.out.println("ItemProcess : " + item);
            return String.valueOf(Integer.valueOf(item) * -1);
        }
    }
}
