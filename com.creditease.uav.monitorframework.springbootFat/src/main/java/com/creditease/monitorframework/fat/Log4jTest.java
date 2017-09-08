package com.creditease.monitorframework.fat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.creditease.monitorframework.fat.log.Logger;
import com.creditease.monitorframework.fat.log.LoggerFactory;

@WebServlet("/Log4jTest")
public class Log4jTest extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Logger logger = null;
    ExecutorService ser = null;

    @Override
    public void init() throws ServletException {

        super.init();
        logger = LoggerFactory.getLogger(Log4jTest.class);
    }

    @Override
    public void destroy() {

        ser.shutdown();
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String type = req.getParameter("type");
        if (type.equals("app_log")) {
            try {
                writeAPPLog("");
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (type.equals("sys_log")) {
            try {
                writeSysLog();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (type.equals("stack_trace")) {
            try {
                writeStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (type.equals("roll")) {
            whileOutLog("out");
        }
        else if (type.equals("app_log_mul")) {
            writeAppLogMulThread();
        }

    }

    private void writeAppLogMulThread() throws IOException {

        if (System.getProperty("os.name").contains("Windows")) {
            File expectOutput1 = new File("D:\\logFolder\\expectOutput1.txt");
            File expectOutput2 = new File("D:\\logFolder\\expectOutput2.txt");
            File expectOutput3 = new File("D:\\logFolder\\expectOutput3.txt");

            MyThread myThread1 = new MyThread(expectOutput1, 1);
            MyThread myThread2 = new MyThread(expectOutput2, 2);
            MyThread myThread3 = new MyThread(expectOutput3, 3);

            myThread1.start();
            myThread2.start();
            myThread3.start();
        }
    }

    private void whileOutLog(String outs) {

        ser = Executors.newSingleThreadExecutor();
        ser.execute(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    try {
                        logger.info("Test HBase insert Log info-" + new Date());
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException e) {
                    }
                }

            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doGet(req, resp);
    }

    @SuppressWarnings("resource")
    public void writeAPPLog(String expectOutPutFilePath) throws IOException, InterruptedException {
        // //used in window
        // File expectOutput = new File("D:\\logFolder\\expectOutput.txt");

        // used in linux
        File expectOutput = new File("/home/work/UAV/Log/expectOutput.txt");
        FileWriter writerExpect = new FileWriter(expectOutput, true);

        // 测试不带标识的情况
        Thread.sleep(10000);
        System.out.println("日志添加aaaaaaaaaaa，不应该抓取\r\n");
        logger.info("aaaaaaaaaaa");

        // 测试正常标识正常内容的情况
        Thread.sleep(10000);
        System.out.println("日志添加[CE]bbbbbbbbbb，应该抓取[CE]bbbbbbbbbb\r\n");
        logger.info("[CE]bbbbbbbbbb");
        writerExpect.write("{\"content1\":\"[CE]bbbbbbbbbb\"}\r\n");
        writerExpect.flush();

        // 测试不正常标识，正常内容的情况
        Thread.sleep(10000);
        System.out.println("日志添加{CE]cccccccccc，不应该抓取\r\n");
        logger.info("{CE]cccccccccc");

        // 测试正常标识，不正常内容的情况
        Thread.sleep(10000);
        System.out.println("日志添加[CE]~!@#$%^&*()qwertyuiopasdfghjklzxcvbnm，应该抓取\r\n");
        logger.info("[CE]~!@#$%^&*()qwertyuiopasdfghjklzxcvbnm");
        writerExpect.write("{\"content1\":\"[CE]~!@#$%^&*()qwertyuiopasdfghjklzxcvbnm\"}\r\n");
        writerExpect.flush();

        // 测试分段截取正常内容的情况
        Thread.sleep(10000);
        System.out.println("日志添加[CE]ddddd\teeeeeee\tfffffffff，应该抓取[CE]ddddd\r\n");
        logger.info("[CE]dddddeeeeeeefffffffff");
        writerExpect.write("{\"content1\":\"[CE]dddddeeeeeeefffffffff\"}\r\n");
        writerExpect.flush();

        // ************规则改成<CE>****************//
        System.out.println("新规则后输入的日志\r\n");

        // 测试新规则下原来会抓取的现在还会不会抓取的情况
        Thread.sleep(20000);
        System.out.println("日志添加[CE]ggggggggg，规则改变，不应该抓取\r\n");
        logger.info("[CE]ggggggggg");

        // 测试新规则下应该抓取的正常情况
        Thread.sleep(10000);
        System.out.println("日志添加<CE>hhhhhhhhhh，应该抓取<CE>hhhhhhhhhh\r\n");
        logger.info("<CE>hhhhhhhhhh");
        writerExpect.write("{\"content1\":\"<CE>hhhhhhhhhh\"}\r\n");
        writerExpect.flush();

        Thread.sleep(10000);
        System.out.println("日志添加<CE>iiiii|jjjjj|kkkkkk\r\n");
        logger.info("<CE>iiiii|jjjjj|kkkkkk");
        writerExpect.write("{\"content1\":\"<CE>iiiii\",\"content2\":\"jjjjj\"}\r\n");
        writerExpect.flush();
        System.out.println("********** the  end of app-log ***********");
    }

    public void writeSysLog() throws InterruptedException, IOException {
        // used in window
        // File expectOutput = new File("D:\\logFolder\\expectOutput.txt");
        // FileWriter writerExpect = new FileWriter(expectOutput,true);

        // used in linux
        File expectOutput = new File("/home/work/UAV/Log/expectOutput.txt");
        @SuppressWarnings("resource")
        FileWriter writerExpect = new FileWriter(expectOutput, true);

        Thread.sleep(10000);
        System.out.println(
                "日志添加Feb 18 14:19:14 localhost sshd[111111]: pam_unix(sshd:session): session closed for user aaaa,应该抓取\r\n");
        logger.info(
                "Feb 18 14:19:14 localhost sshd[111111]: pam_unix(sshd:session): session closed for user aaaa1111111");
        writerExpect.write(
                "{\"message\":\"pam_unix(sshd:session): session closed for user aaaa1111111\",\"host\":\"localhost\",\"pid\":\"sshd[111111]:\",\"date\":\"Feb 18 14:19:14\"}\r\n");
        writerExpect.flush();

        Thread.sleep(10000);
        System.out.println("日志添加localhost sshd[22222]: subsystem request for sftp，不应该抓取\r\n");
        logger.info("localhost sshd[22222]: subsystem request for sftp");

        Thread.sleep(10000);
        System.out.println(
                "日志添加Feb 19 18:07:59 localhost sshd[3333]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh222222，应该抓取\r\n");
        logger.info(
                "Feb 19 18:07:59 localhost sshd[3333]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh2");
        writerExpect.write(
                "{\"message\":\"Accepted password for aaaa from 127.0.0.1 port 63869 ssh2\",\"host\":\"localhost\",\"pid\":\"sshd[3333]:\",\"date\":\"Feb 19 18:07:59\"}\r\n");
        writerExpect.flush();

        Thread.sleep(10000);
        System.out.println("日志添加subsystem[44444] request for sftp，不应该抓取\r\n");
        logger.info("subsystem[44444] request for sftp");

        Thread.sleep(10000);
        System.out.println("日志添加aaaaaaa[555555]  sdasdasd subsystem: request for sftp，不应该抓取\r\n");
        logger.info("aaaaaaa[555555]  sdasdasd subsystem: request for sftp");

        Thread.sleep(10000);
        System.out.println(
                "日志添加aaaaaac[6666666] Feb 19 18:07:59 localhost sshd[]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh2，不应该抓取\r\n");
        logger.info(
                "aaaaaac[6666666] Feb 19 18:07:59 localhost sshd[]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh2");

        Thread.sleep(10000);
        System.out.println(
                "日志添加aaaaaac[7777777] Feb 19 18:07:59 localhost sshd[]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh2，不应该抓取\r\n");
        logger.info(
                "Feb[7777777] 19 18:07:59 localhost sshd[]: Accepted password for aaaa from 127.0.0.1 port 63869 ssh2");

        Thread.sleep(10000);
        System.out.println("日志添加Jan 20 15:15:42 localhost sshd[888888]: Exiting on signal 15，应该抓取\r\n");
        logger.info("Jan 20 15:15:42 localhost sshd[888888]: Exiting on signal 15");
        writerExpect.write(
                "{\"message\":\"Exiting on signal 15\",\"host\":\"localhost\",\"pid\":\"sshd[888888]:\",\"date\":\"Jan 20 15:15:42\"}\r\n");
        writerExpect.flush();

        System.out.println("********** the end of sys-log **************** ");
    }

    public void writeStackTrace() throws IOException, InterruptedException {
        // used in window
        // File expectOutput = new File("D:\\logFolder\\expectOutput.txt");
        // FileWriter writerExpect = new FileWriter(expectOutput,true);

        // used in linux
        File expectOutput = new File("/home/work/UAV/Log/expectOutput.txt");
        @SuppressWarnings("resource")
        FileWriter writerExpect = new FileWriter(expectOutput, true);

        Thread.sleep(10000);
        logger.error("IOexception", new IOException("1111111111 failed.."));
        System.out.println(" IOexception >>>>  111111111");
        writerExpect.write(
                "{\"stacktrace\":\"java.io.IOException: 1111111111 failed..\\tat com.creditease.monitorframework.fat.Log4jTest.writeStackTrace(Log4jTest.java:191)\\tat com.creditease.monitorframework.fat.Log4jTest.doGet(Log4jTest.java:48)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:624)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:731)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:307)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:245)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:220)\\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:122)\\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:505)\\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:170)\\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)\\tat org.apache.catalina.valves.AccessLogValve.invoke(AccessLogValve.java:956)\\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)\\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:443)\\tat org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1079)\\tat org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)\\tat org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\\tat java.lang.Thread.run(Thread.java:745)\"}\r\n");
        writerExpect.flush();

        Thread.sleep(10000);
        logger.error("InterruptedException", new InterruptedException("222222222 faild.."));
        System.out.println("InterruptedException >>> 222222222");
        writerExpect.write(
                "{\"stacktrace\":\"java.lang.InterruptedException: 222222222 faild..\\tat com.creditease.monitorframework.fat.Log4jTest.writeStackTrace(Log4jTest.java:197)\\tat com.creditease.monitorframework.fat.Log4jTest.doGet(Log4jTest.java:48)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:624)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:731)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:307)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:245)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:220)\\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:122)\\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:505)\\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:170)\\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)\\tat org.apache.catalina.valves.AccessLogValve.invoke(AccessLogValve.java:956)\\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)\\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:443)\\tat org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1079)\\tat org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)\\tat org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\\tat java.lang.Thread.run(Thread.java:745)\"}\r\n");
        writerExpect.flush();

        Thread.sleep(10000);
        logger.error("ClassNotFoundException", new ClassNotFoundException("33333333"));
        System.out.println("ClassNotFoundException >>> 33333333");
        writerExpect.write(
                "{\"stacktrace\":\"java.lang.ClassNotFoundException: 33333333\\tat com.creditease.monitorframework.fat.Log4jTest.writeStackTrace(Log4jTest.java:203)\\tat com.creditease.monitorframework.fat.Log4jTest.doGet(Log4jTest.java:48)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:624)\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:731)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:307)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)\\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:245)\\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:212)\\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:220)\\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:122)\\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:505)\\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:170)\\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)\\tat org.apache.catalina.valves.AccessLogValve.invoke(AccessLogValve.java:956)\\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)\\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:443)\\tat org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1079)\\tat org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)\\tat org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)\\tat java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)\\tat java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)\\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)\\tat java.lang.Thread.run(Thread.java:745)\"}\r\n");
        writerExpect.flush();

        System.out.println("*********** the end of stack trace *********");

    }

}

class MyThread extends Thread {

    FileWriter writer = null;
    org.apache.log4j.Logger logger = null;
    int loggerIndex = -1;

    public MyThread(File file, int loggerIndex) throws IOException {
        writer = new FileWriter(file, true);
        logger = org.apache.log4j.Logger.getLogger("logger" + loggerIndex);
        this.loggerIndex = loggerIndex;
    }

    public void writeAppLog() throws IOException, InterruptedException {

        // 测试不带标识的情况
        Thread.sleep(10000);
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 1111111111111111");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 2222222222222222");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 3333333333333333");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 4444444444444444");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 5555555555555555");
        System.out.println(Thread.currentThread().getId()
                + " >> [CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 1-5\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 1111111111111111\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 2222222222222222\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 3333333333333333\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 4444444444444444\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 5555555555555555\r\n");
        writer.flush();
        // 测试正常标识正常内容的情况
        Thread.sleep(10000);
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 666666666666666");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 777777777777777");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 888888888888888");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 999999999999999");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 0000000000000000");
        System.out.println(Thread.currentThread().getId()
                + " >> [CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 6-0\r\n");

        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 666666666666666\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 777777777777777\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 888888888888888\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 999999999999999\r\n");
        writer.write(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success 0000000000000000\r\n");
        writer.flush();

        // 改变规则后
        Thread.sleep(60000);
        logger.info(
                "com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success aaaaaaaaaaaaaaaaa");
        logger.info(
                "com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success bbbbbbbbbbbbbbbbb");
        logger.info(
                "com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ccccccccccccccccc");
        logger.info(
                "com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ddddddddddddddddd");
        logger.info(
                "com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success eeeeeeeeeeeeeeeeee");
        System.out.println(Thread.currentThread().getId()
                + " >> [CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success a-e\r\n");

        // 测试正常标识，不正常内容的情况
        Thread.sleep(10000);

        // 适用规则是[CE]的
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii");
        logger.info(
                "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj");

        // 适合规则为[CA]的
        logger.info(
                "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff");
        logger.info(
                "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg");
        logger.info(
                "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh");
        logger.info(
                "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii");
        logger.info(
                "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj");

        // 适合规则为[CB]的
        logger.info(
                "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff");
        logger.info(
                "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg");
        logger.info(
                "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh");
        logger.info(
                "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii");
        logger.info(
                "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj");
        System.out.println(Thread.currentThread().getId()
                + " >> [CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success f-j\r\n");
        if (loggerIndex == 1) {
            writer.write(
                    "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff\r\n");
            writer.write(
                    "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg\r\n");
            writer.write(
                    "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh\r\n");
            writer.write(
                    "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii\r\n");
            writer.write(
                    "[CE]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj\r\n");
            writer.flush();
        }
        else if (loggerIndex == 2) {
            writer.write(
                    "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff\r\n");
            writer.write(
                    "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg\r\n");
            writer.write(
                    "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh\r\n");
            writer.write(
                    "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii\r\n");
            writer.write(
                    "[CA]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj\r\n");
            writer.flush();
        }
        else {
            writer.write(
                    "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success fffffffffffffffffff\r\n");
            writer.write(
                    "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success ggggggggggggggggggg\r\n");
            writer.write(
                    "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success hhhhhhhhhhhhhhhhhhh\r\n");
            writer.write(
                    "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success iiiiiiiiiiiiiiiiiii\r\n");
            writer.write(
                    "[CB]-75 com.creditease.digitsign.dss.bo.SavePushInfoToDB.doSaveBorrowInfo(161): reqeustId: ZHXD666666systemId: ICPGet borrowInfo from req success jjjjjjjjjjjjjjjjjjj\r\n");
            writer.flush();
        }

        // // 测试分段截取正常内容的情况
        // Thread.sleep(10000);
        // System.out.println(Thread.currentThread().getId() + "
        // >>日志添加[CE]ddddd\teeeeeee\tfffffffff，应该抓取[CE]ddddd\r\n");
        // logger.info("[CE]dddddeeeeeeefffffffff");
        // writer.write("{\"content1\":\"[CE]dddddeeeeeeefffffffff\"}\r\n");
        // writer.flush();
    }

    @Override
    public void run() {

        try {
            writeAppLog();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}