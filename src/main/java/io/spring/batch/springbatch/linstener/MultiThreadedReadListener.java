package io.spring.batch.springbatch.linstener;

import io.spring.batch.springbatch.dto.Customer3;
import org.springframework.batch.core.ItemReadListener;

public class MultiThreadedReadListener implements ItemReadListener<Customer3> {
    @Override
    public void beforeRead() {

    }

    @Override
    public void afterRead(Customer3 customer3) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " / read item : " + customer3.getId());
    }

    @Override
    public void onReadError(Exception e) {

    }
}
