package io.spring.batch.springbatch.example1.chunk.processor;

import io.spring.batch.springbatch.example1.domain.Product;
import io.spring.batch.springbatch.example1.domain.ProductVo;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class FileItemProcessor implements ItemProcessor<ProductVo, Product> {
    @Override
    public Product process(ProductVo item) throws Exception {
        ModelMapper modelMapper = new ModelMapper();
        Product product = modelMapper.map(item, Product.class);
        return product;
    }
}
