package com.creditease.uav.monitorframework.springbootFat.log;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("log4j")
public class Log4jTest {

    // 测试此测试用例时，需要将springboot的日志输出转换为logback，对应pom文件注释位置
    // private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // // private static final Logger logger = Logger.getLogger(Log4jTest.class);
    //
    // @RequestMapping("writelog")
    // public String writeLog() {
    //
    // logger.debug("This is a debug message");
    // logger.info("This is an info message");
    // logger.warn("This is a warn message");
    // logger.error("This is an error message");
    // try {
    // throw new IOException("异常测试");
    // }
    // catch (IOException e) {
    // // TODO Auto-generated catch block
    // logger.error("test exception", e);
    // }
    // return "OK";
    // }
}
