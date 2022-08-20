package io.spring.batch.springbatch.linstener;

import org.springframework.batch.core.ItemProcessListener;

public class CustomItemProcessListener implements ItemProcessListener<Integer, String> {
    @Override
    public void beforeProcess(Integer integer) {
        System.out.println("beforeProcess: " + integer);
    }

    @Override
    public void afterProcess(Integer integer, String s) {
        System.out.println("afterProcess: " + integer + ", " + s);

    }

    @Override
    public void onProcessError(Integer integer, Exception e) {
        System.out.println("onProcessError: " + integer + ", " + e);
    }
}
