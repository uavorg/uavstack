package com.creditease.monitorframework.fat.log;

public class LoggerFactory {

    private static final Logger logger = new Logger();

    public static Logger getLogger(Class<?> c) {

        return logger;
    }
}
