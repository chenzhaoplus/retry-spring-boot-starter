package com.smcaiot.retry.starter;

import com.smcaiot.retry.starter.app.RetryAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = RetryAutoConfiguration.class)
public class MyTest {

    private final static Logger logger = LoggerFactory.getLogger(MyTest.class);

    @Test
    public void test() throws IOException, InterruptedException {
        logger.info("启动成功！");
        for (; ; ) {
            Thread.sleep(60000);
        }
    }

}
