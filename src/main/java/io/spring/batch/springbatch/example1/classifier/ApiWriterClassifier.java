package io.spring.batch.springbatch.example1.classifier;

import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

public class ApiWriterClassifier<C, T> implements Classifier<C, T> {
    private Map<String, ItemWriter<ApiRequestVo>> writerMap = new HashMap<>();

    @Override
    public T classify(C classifiable) {
        return (T)writerMap.get(((ApiRequestVo)classifiable).getProductVo().getType());

    }

    public void setProcessorMap(Map<String, ItemWriter<ApiRequestVo>> writerMap) {
        this.writerMap = writerMap;
    }
}
