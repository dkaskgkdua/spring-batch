package io.spring.batch.springbatch.reader;

import io.spring.batch.springbatch.exception.SkippableException;
import org.springframework.aop.support.AopUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;

public class LinkedListItemReader<T> implements ItemReader<T> {
    private List<T> list;

    public LinkedListItemReader(List<T> list) {
        if(AopUtils.isAopProxy(list)) {
            System.out.println("프록시 ok");
            this.list = list;
        } else {
            System.out.println("프록시 no");
            this.list = new LinkedList<>(list);
        }
    }

    @Nullable
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println(list);
        if(!list.isEmpty()) {
            T remove = (T)list.remove(0);
            if((Integer)remove == 3) {
                System.out.println("read skip!!!");
                throw new SkippableException("read skipped : " + remove);
            }
            return remove;
        }
        return null;
    }
}
