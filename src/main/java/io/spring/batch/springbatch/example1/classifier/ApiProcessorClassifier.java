package io.spring.batch.springbatch.example1.classifier;

import io.spring.batch.springbatch.example1.domain.ApiRequestVo;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import io.spring.batch.springbatch.processor.ProcessorInfo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

public class ApiProcessorClassifier<C, T> implements Classifier<C, T> {
    private Map<String, ItemProcessor<ProductVo, ApiRequestVo>> processorMap = new HashMap<>();

    @Override
    public T classify(C classifiable) {
        return (T)processorMap.get(((ProductVo)classifiable).getType());

    }

    public void setProcessorMap(Map<String, ItemProcessor<ProductVo, ApiRequestVo>> processorMap) {
        this.processorMap = processorMap;
    }
}
