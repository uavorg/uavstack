/*-
 * <<
 * UAVStack
 * ==
 * Copyright (C) 2016 - 2017 UAVStack
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.creditease.uav.hook.jdbc.invokeChain;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import com.creditease.agent.helpers.DataConvertHelper;
import com.creditease.monitor.UAVServer;
import com.creditease.monitor.captureframework.spi.CaptureConstants;
import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainAdapter;
import com.creditease.uav.apm.invokechain.spi.InvokeChainConstants;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.slowoper.spi.SlowOperConstants;
import com.creditease.uav.apm.slowoper.spi.SlowOperContext;

public class JdbcDriverAdapter extends InvokeChainAdapter {

    @Override
    public void beforePreCap(InvokeChainContext context, Object[] args) {

        // 查找应用类信息所需的关键类名和相隔层数
        context.put(InvokeChainConstants.CLIENT_IT_KEY,
                DataConvertHelper.toInt(System.getProperty("com.creditease.uav.invokechain.code.jdbc.key"), 0));
        context.put(InvokeChainConstants.CLIENT_IT_CLASS,
                System.getProperty("com.creditease.uav.invokechain.code.jdbc.class"));
    }

    @Override
    public void afterPreCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            @SuppressWarnings("unchecked")
            String sql = sqlParse(args[0].toString(), (List<Map<Integer, String>>) args[1]);
            SlowOperContext slowOperContext = new SlowOperContext();
            slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_SQL, sql);

            String url = (String) context.get(InvokeChainConstants.CLIENT_SPAN_THREADLOCAL_STOREKEY);
            Span span = this.spanFactory.getSpanFromContext(url);

            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.PRECAP, context, params);
        }

    }

    @Override
    public void beforeDoCap(InvokeChainContext context, Object[] args) {

        // 当前sql执行
        String action = (String) context.get(CaptureConstants.INFO_CLIENT_REQUEST_ACTION);
        // 在原CaptureConstants.INFO_CLIENT_RESPONSESTATE追加操作类型
        if (context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE) == null) {
            context.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, action);
        }
        else {
            // 不为空则在status之前追加操作类型
            String status = (String) context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE);
            context.put(CaptureConstants.INFO_CLIENT_RESPONSESTATE, action + "?ERR" + status);
        }
    }

    @Override
    public void afterDoCap(InvokeChainContext context, Object[] args) {

        if (UAVServer.instance().isExistSupportor("com.creditease.uav.apm.supporters.SlowOperSupporter")) {
            Span span = (Span) context.get(InvokeChainConstants.PARAM_SPAN_KEY);
            SlowOperContext slowOperContext = new SlowOperContext();
            if (context.get(CaptureConstants.INFO_CLIENT_RESPONSECODE).toString().equals("-1")) {
                slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT,
                        context.get(CaptureConstants.INFO_CLIENT_RESPONSESTATE));
            }
            else {
                Method method = (Method) args[0];
                if (method.getName().equals("execute")) {
                    boolean res = (Boolean) args[1];
                    if (res) {
                        Statement statement = (Statement) args[2];
                        try {
                            slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT,
                                    sqlResultSetParse(statement.getResultSet()));
                        }
                        catch (SQLException e) {
                            // 由于rc的值即可判断是否有异常，故ignore
                        }
                    }
                    else {
                        Statement statement = (Statement) args[2];
                        try {
                            slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT, statement.getUpdateCount());
                        }
                        catch (SQLException e) {
                            // 由于rc的值即可判断是否有异常，故ignore
                        }
                    }
                }
                else if (method.getName().equals("executeBatch")) {
                    int[] res = (int[]) args[1];
                    StringBuilder builder = new StringBuilder("[ ");
                    for (int value : res) {
                        builder.append(value + " ");
                    }
                    builder.append("]");
                    slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT, builder.toString());
                }
                else {
                    Statement statement = (Statement) args[2];
                    try {
                        slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT, statement.getUpdateCount());
                    }
                    catch (Exception e) {
                        slowOperContext.put(SlowOperConstants.PROTOCOL_JDBC_RESULT, method.getName() + " unsupport!!!");
                    }
                }
            }
            Object params[] = { span, slowOperContext };
            UAVServer.instance().runSupporter("com.creditease.uav.apm.supporters.SlowOperSupporter", "runCap",
                    span.getEndpointInfo().split(",")[0], InvokeChainConstants.CapturePhase.DOCAP, context, params);
        }

    }

    /**
     * 还原sql该有的面貌
     * 
     * @param sql
     * @param parameters
     * @return
     */
    private String sqlParse(String sql, List<Map<Integer, String>> parameters) {

        if (parameters.isEmpty()) {
            return sql;
        }
        // 当前解析效率比较低，日后会根据mysql driver修改一版
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Map<Integer, String> map = parameters.get(i);
            if (map.isEmpty()) {
                continue;
            }
            String str = sql;
            int key = 1;
            for (int j = 0; j < str.length(); j++) {
                char item = str.charAt(j);
                if (item == '?') {
                    result.append(map.get(key++));
                }
                else {
                    result.append(item);
                }
                if (j == str.length() - 1) {
                    result.append(';');
                }
            }
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : sql;
    }

    /**
     * 解析resultset中的条数
     * 
     * @param resultSet
     * @return
     */
    private String sqlResultSetParse(ResultSet rset) {

        try {
            // 只能向前遍历的游标，只能用next()来遍历
            if (rset.getType() == ResultSet.TYPE_FORWARD_ONLY) {
                return "FORWARD_ONLY";
            }
            else {
                // 可以滚动的游标，在用户使用游标之前，直接获取结果集大小，然后游标回到开始的地方（假装游标没有移动过）
                rset.last();
                int row = rset.getRow();
                rset.beforeFirst();
                return row + "";

            }
        }
        catch (SQLException e) {
            return e.toString();
        }
    }

}
