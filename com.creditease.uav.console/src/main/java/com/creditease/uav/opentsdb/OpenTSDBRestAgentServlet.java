package com.creditease.uav.opentsdb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.creditease.agent.helpers.JSONHelper;
import com.creditease.agent.log.SystemLogger;
import com.creditease.agent.log.api.ISystemLogger;
import com.creditease.uav.httpasync.HttpAsyncClient;
import com.creditease.uav.httpasync.HttpAsyncClientFactory;
import com.creditease.uav.httpasync.HttpClientCallback;
import com.creditease.uav.httpasync.HttpClientCallbackResult;

public class OpenTSDBRestAgentServlet extends HttpServlet {

    private static HttpAsyncClient httpAsyncClient = null;

    private static final long serialVersionUID = 1441879223883391706L;

    private static Map<String, String> info = null;

    private static String url;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {

        if (null == info) {
            String esInfo = this.getInitParameter("db.info");
            info = JSONHelper.toObject(esInfo, Map.class);
        }

        if (null == httpAsyncClient) {
            Map<String, Integer> httpParamsMap = JSONHelper
                    .toObject(getServletContext().getInitParameter("uav.app.es.http.client.params"), Map.class);
            httpAsyncClient = HttpAsyncClientFactory.build(httpParamsMap.get("max.con"),
                    httpParamsMap.get("max.tot.con"), httpParamsMap.get("sock.time.out"),
                    httpParamsMap.get("con.time.out"), httpParamsMap.get("req.time.out"));
        }
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("////////////////////////////");
        String para_url = req.getParameter("url");
        if (null != para_url) {
            url = para_url;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        // 获取请求资源
        String proName = req.getServletContext().getContextPath();
        String requestSource = req.getRequestURI();
        requestSource = requestSource.substring(requestSource.indexOf(proName) + proName.length());
        String queryString = req.getQueryString();

        if (requestSource.startsWith("/db2")) {
            requestSource = requestSource.substring(4);
        }
        if (requestSource.contains("queryDb")) {
            requestSource = "/";
        }
        /**
         * get url
         */
        String forwarUrl;
        if (queryString != null && !queryString.contains("url=")) {
            forwarUrl = url + requestSource + "?" + queryString;
        }
        else {
            forwarUrl = url + requestSource;
        }
        /**
         * get method
         */
        String method = req.getMethod();
        /**
         * get body
         */
        ServletInputStream input = req.getInputStream();
        httpAsyncClient.doAsyncHttpMethodWithReqAsync(method, forwarUrl, null, input, null, null, "application/json",
                "utf-8", new OpenTSDBRestAgentServletCallBack(resp, forwarUrl), req);

    }

}

class OpenTSDBRestAgentServletCallBack implements HttpClientCallback {

    private HttpServletResponse resp;
    private String url;

    public OpenTSDBRestAgentServletCallBack(HttpServletResponse resp, String forwarUrl) {
        super();
        this.resp = resp;
        this.url = forwarUrl;
    }

    @Override
    public void completed(HttpClientCallbackResult result) {

        resp(result);
    }

    @Override
    public void failed(HttpClientCallbackResult result) {

        resp(result);
    }

    private void resp(HttpClientCallbackResult result) {

        if (null != result.getException()) {
            ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestAgentServletCallBack.class);
            logger.err(this, result.getException().getMessage(), result.getException());
        }
        else {
            String respStr = result.getReplyDataAsString();
            if (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith("&png")) {
                downLoadImage(resp, url);
                return;
            }
            try {
                result.getResponseForRequestAsync().write(respStr.getBytes("UTF-8"));
                result.getResponseForRequestAsync().flush();
                result.getResponseForRequestAsync().close();
            }
            catch (IOException e) {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestAgentServletCallBack.class);
                logger.err(this, result.getException().getMessage(), result.getException());
            }

        }

    }

    private void downLoadImage(HttpServletResponse resp, String url) {

        HttpClient client = new HttpClient();
        GetMethod get = null;
        try {
            String path = url;
            get = new GetMethod(path);
            int i = client.executeMethod(get);
            if (200 == i) {
                resp.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(path, "UTF-8"));
                OutputStream out = null;
                out = resp.getOutputStream();
                out.write(get.getResponseBody());
                out.flush();
                out.close();
            }
            else {
                ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestAgentServletCallBack.class);
                logger.err(this, "no pic");
            }
        }
        catch (Exception e) {
            ISystemLogger logger = SystemLogger.getLogger(OpenTSDBRestAgentServletCallBack.class);
            logger.err(this, e.getMessage());
        }
        finally {
            get.releaseConnection();
            client.getHttpConnectionManager().closeIdleConnections(0);
        }
    }
}
