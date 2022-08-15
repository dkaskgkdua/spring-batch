package io.spring.batch.springbatch.linstener;

import io.spring.batch.springbatch.dto.Customer3;
import org.springframework.batch.core.ItemProcessListener;

public class MultiThreadedProcessListener implements ItemProcessListener<Customer3,Customer3> {
    @Override
    public void beforeProcess(Customer3 o) {

    }

    @Override
    public void afterProcess(Customer3 customer3, Customer3 result) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " / process item : " + customer3.getId());
    }

    @Override
    public void onProcessError(Customer3 o, Exception e) {

    }

}
