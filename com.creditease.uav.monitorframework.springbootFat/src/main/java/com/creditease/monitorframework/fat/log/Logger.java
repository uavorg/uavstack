package com.creditease.monitorframework.fat.log;

public class Logger {

    private final org.slf4j.Logger DLogger;

    public Logger() {
        DLogger = org.slf4j.LoggerFactory.getLogger(Logger.class);
    }

    public boolean isDebugEnabled() {

        return DLogger.isDebugEnabled();
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     */
    public void debug(String requestId, String systemId, String operation, String msg) {

        DLogger.debug(formatMsg(formatRequestMsg(requestId, systemId, operation, msg)));
    }

    public void debug(String msg) {

        DLogger.debug(formatMsg(msg));
    }

    public void debug(String msg, Throwable t) {

        DLogger.debug(formatMsg(msg), t);
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     * @param t
     *            异常抛出
     */
    public void debug(String requestId, String systemId, String operation, String msg, Throwable t) {

        DLogger.debug(formatMsg(formatRequestMsg(requestId, systemId, operation, msg)), t);
    }

    public boolean isInfoEnabled() {

        return DLogger.isInfoEnabled();
    }

    public void info(String msg) {

        DLogger.info(formatMsg(msg));
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     */
    public void info(String requestId, String systemId, String operation, String msg) {

        DLogger.info(formatMsg(formatRequestMsg(requestId, systemId, operation, msg)));
    }

    public boolean isWarnEnabled() {

        return DLogger.isWarnEnabled();
    }

    public void warn(String msg) {

        DLogger.warn(formatMsg(msg));
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     */
    public void warn(String requestId, String systemId, String operation, String msg) {

        DLogger.warn(formatMsg(formatRequestMsg(requestId, systemId, operation, msg)));
    }

    public void warn(String msg, Throwable t) {

        DLogger.warn(formatMsg(msg), t);
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     * @param t
     *            异常抛出
     */
    public void warn(String requestId, String systemId, String operation, String msg, Throwable t) {

        DLogger.warn(formatMsg(formatRequestMsg(requestId, systemId, operation, msg)), t);
    }

    public boolean isErrorEnabled() {

        return DLogger.isErrorEnabled();
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     * @param errCode
     *            错误代码
     * @param errCodeMsg
     *            错误信息
     * @param t
     *            异常
     */
    public void error(String requestId, String systemId, String operation, String msg, String errCode,
            String errCodeMsg, Throwable t) {

        String errmsg = formatRequestMsg(requestId, systemId, operation, msg) + formatErrCode(errCode, errCodeMsg);
        errmsg = formatMsg(errmsg);
        DLogger.error(errmsg, t);
    }

    /**
     * 
     * @param msg
     * @param errCode
     * @param errCodeMsg
     * @param t
     */
    public void error(String msg, String errCode, String errCodeMsg, Throwable t) {

        String errmsg = formatMsg(msg + formatErrCode(errCode, errCodeMsg));
        DLogger.error(errmsg, t);
    }

    /**
     * 
     * @param msg
     * @param t
     */
    public void error(String msg, Throwable t) {

        String errmsg = formatMsg(msg);
        DLogger.error(errmsg, t);
    }

    /**
     * 
     * @param requestId
     *            请求ID
     * @param systemId
     *            系统标识（可选）
     * @param operation
     *            操作
     * @param msg
     *            日志信息
     * @param t
     *            异常抛出
     */
    public void error(String requestId, String systemId, String operation, String msg, Throwable t) {

        String errmsg = formatMsg(formatRequestMsg(requestId, systemId, operation, msg));
        DLogger.error(errmsg, t);
    }

    private String formatMsg(String msg) {

        StringBuilder sb = new StringBuilder();

        // sb.append("[CE]
        // -").append(Thread.currentThread().getId()).append("\t").append(getCallerMethod(4)).append(":\t").append(msg);
        sb.append(msg);
        return sb.toString();
    }

    private String formatErrCode(String errCode, String errCodeMsg) {

        StringBuilder sb = new StringBuilder();
        return sb.append("\terrCode=").append(errCode).append("\terrMsg=").append(errCodeMsg).toString();
    }

    private String formatRequestMsg(String requestId, String systemId, String operation, String msg) {

        StringBuilder sb = new StringBuilder();

        String system = "";
        if (null != systemId) {
            system = systemId + ":";
        }

        sb.append("<").append(system).append(requestId).append(">\t").append(operation).append("\t").append(msg);

        return sb.toString();
    }

    @SuppressWarnings("unused")
    private String getCallerMethod(int level) {

        int l = 3;

        if (level > 0)
            l = level;

        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        StackTraceElement a = temp[l];

        return a.getClassName() + "." + a.getMethodName() + "(" + a.getLineNumber() + ")";
    }
}