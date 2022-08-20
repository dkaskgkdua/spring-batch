package io.spring.batch.springbatch.linstener;

import org.springframework.batch.core.ItemReadListener;

public class CustomItemReadListener implements ItemReadListener {
    @Override
    public void beforeRead() {
        System.out.println("beforeRead");
    }

    @Override
    public void afterRead(Object o) {
        System.out.println("afterRead");

    }

    @Override
    public void onReadError(Exception e) {
        System.out.println("onReadError");
    }
}
