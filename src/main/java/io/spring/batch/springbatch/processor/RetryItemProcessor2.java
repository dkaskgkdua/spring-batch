package io.spring.batch.springbatch.processor;

import io.spring.batch.springbatch.dto.Customer3;
import io.spring.batch.springbatch.dto.ItemCustomer;
import io.spring.batch.springbatch.exception.RetryableException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.classify.Classifier;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;

public class RetryItemProcessor2 implements ItemProcessor<String, ItemCustomer> {
    @Autowired
    private RetryTemplate retryTemplate;

    private int cnt = 0;

    @Override
    public ItemCustomer process(String item) throws Exception {
        Classifier<Throwable, Boolean> rollbackClassifier = new BinaryExceptionClassifier(true);

        ItemCustomer customer = retryTemplate.execute(new RetryCallback<ItemCustomer, RuntimeException>() {
            @Override
            public ItemCustomer doWithRetry(RetryContext retryContext) throws RuntimeException {
                if(item.equals("1") || item.equals("2")) {
                    cnt++;
                    throw new RetryableException("failed cnt : " + cnt);
                }
                return new ItemCustomer(item);
            }
        }, new RecoveryCallback<ItemCustomer>() {
            @Override
            public ItemCustomer recover(RetryContext retryContext) throws Exception {
                /**
                 * 기존 정의된 RecoveryCallback은 skip과 같은 옵션들이 적용되어
                 * process 실행 시 발생하는 에러에 skip을 실행하지만
                 * 아래와 같이 사용자 정의일 경우 에러가 발생해서 콜백으로 오면
                 * 커스텀이 가능하다.
                 * 한마디로! 장애 대응책임.
                 */
                return new ItemCustomer(item);
            }
        }, new DefaultRetryState(item, rollbackClassifier));
        return customer;
    }
}
