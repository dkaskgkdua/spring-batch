package io.spring.batch.springbatch.example1.service;

import io.spring.batch.springbatch.example1.domain.ApiInfo;
import io.spring.batch.springbatch.example1.domain.ApiResponseVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService1 extends AbstractApiService{
    @Override
    protected ApiResponseVo doApiService(RestTemplate restTemplate, ApiInfo apiInfo) {
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8081/api/product/1", apiInfo, String.class);
        int statusCodeValue = responseEntity.getStatusCodeValue();
        ApiResponseVo apiResponseVo = ApiResponseVo.builder().status(statusCodeValue).msg(responseEntity.getBody()).build();

        return apiResponseVo;
    }
}