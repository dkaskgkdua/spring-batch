package io.spring.batch.springbatch.processor;

import io.spring.batch.springbatch.exception.RetryableException;
import org.springframework.batch.item.ItemProcessor;

public class RetryItemProcessor implements ItemProcessor<String, String> {
    private int cnt = 0;

    /**
     * 현재 retry 제한 2, skip 제한 2 이다.
     * 아래 item이 2일 경우 exception이 발생하고
     * skip으로 포함된 exception이면 패스하고 retry 시도가 되면서
     * 다음 번호로 넘어간다.
     * skip 설정이 없으면 process는 read에서 처음부터 가져오기 다시 가져오기 때문에
     * 계속 2에 걸려 exception이 발생한다.
     */

    @Override
    public String process(String item) throws Exception {
        if(item.equals("2") || item.equals("3")) {
            cnt ++;
            throw new RetryableException("failed cnt : " + cnt);
        }
        return item;
    }
}
