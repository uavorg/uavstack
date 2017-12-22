package com.creditease.uav.monitorframework.springbootFat.log;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("logback")
public class LogbackTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("writelog")
    public String writeLog() {

        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warn("This is a warn message");
        logger.error("This is an error message");
        try {
            throw new IOException("异常测试");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("test exception", e);
        }
        return "OK";
    }
}
