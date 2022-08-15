package io.spring.batch.springbatch.linstener;

import io.spring.batch.springbatch.dto.Customer3;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;

public class MultiThreadedWriterListener implements ItemWriteListener<Customer3> {
    @Override
    public void beforeWrite(List<? extends Customer3> list) {

    }

    @Override
    public void afterWrite(List<? extends Customer3> list) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " / write items : " + list.size());
    }

    @Override
    public void onWriteError(Exception e, List<? extends Customer3> list) {

    }


}
