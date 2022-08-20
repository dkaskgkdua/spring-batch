package io.spring.batch.springbatch.linstener;

import javax.batch.api.chunk.listener.ItemWriteListener;
import java.util.List;

public class CustomItemWriterListener implements ItemWriteListener {
    @Override
    public void beforeWrite(List<Object> list) throws Exception {
        System.out.println("beforeWrite: " + list.size());
    }

    @Override
    public void afterWrite(List<Object> list) throws Exception {
        System.out.println("    afterWrite ");
    }

    @Override
    public void onWriteError(List<Object> list, Exception e) throws Exception {
        System.out.println("    onWriteError: " + e.getMessage());
    }
}
