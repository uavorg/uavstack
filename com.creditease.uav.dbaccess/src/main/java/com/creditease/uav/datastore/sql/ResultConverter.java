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

package com.creditease.uav.datastore.sql;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class ResultConverter {

    public <T> T convert(ResultSet rs, Class<T> cls) throws SQLException {

        T obj = null;
        boolean isNull = true;
        try {

            ResultSetMetaData md = rs.getMetaData();
            List<String> list = new ArrayList<String>();
            for (int j = 1; j <= md.getColumnCount(); j++) {
                list.add(md.getColumnName(j));
            }

            obj = cls.newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                String columnName = convertColumnName(fieldName);
                if (list.contains(columnName)) {
                    isNull = false;
                    @SuppressWarnings("rawtypes")
                    Class fieldType = field.getType();
                    field.setAccessible(true);
                    field.set(obj, convertColumnValue(fieldType.getName(), columnName, rs));
                }
            }

        }
        catch (Exception e) {
            // logger.error("Convert DB Data FAIL", e);
        }
        return isNull ? null : obj;
    }

    public abstract String convertColumnName(String fieldName) throws SQLException;

    public abstract Object convertColumnValue(String javaType, String columnName, ResultSet rs) throws SQLException;

}
