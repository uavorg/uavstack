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

package com.creditease.agent.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AccessUtil helps to modify the access authority, in case the protected method authority restriction
 * 
 * @author Zhen Zhang
 *
 */
public class AccessHelper {

    private AccessHelper() {
    }

    public static Object authConstructorInvoke(Class<?> c, Class<?>[] paramTypes, Object[] params) {

        try {

            Constructor<?> con = c.getConstructor(paramTypes);
            con.setAccessible(true);
            return con.newInstance(params);
        }
        catch (NoSuchMethodException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (SecurityException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Object authMethodInvoke(Class<?> c, String methodName, Class<?>[] paramTypes, Object instance,
            Object[] params) {

        try {
            Method m = c.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return m.invoke(instance, params);
        }
        catch (NoSuchMethodException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (SecurityException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
        return null;

    }

    public static Object authStaticFieldGet(Class<?> c, String fieldName) {

        try {
            Field f = c.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(c);
        }
        catch (NoSuchFieldException e) {
            // ignore
        }
        catch (SecurityException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        return null;
    }

    public static void authStaticFieldSet(Class<?> c, String fieldName, Object fieldValue) {

        try {
            Field f = c.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(c, fieldValue);
        }
        catch (NoSuchFieldException e) {
            // ignore
        }
        catch (SecurityException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }
    }

    public static Object authFieldGet(Class<?> c, Object o, String fieldName) {

        try {
            Field f = c.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(o);
        }
        catch (NoSuchFieldException e) {
            // ignore
        }
        catch (SecurityException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }

        return null;
    }

    public static void authFieldSet(Class<?> c, Object o, String fieldName, Object fieldValue) {

        try {
            Field f = c.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(o, fieldValue);
        }
        catch (NoSuchFieldException e) {
            // ignore
        }
        catch (SecurityException e) {
            // ignore
        }
        catch (IllegalArgumentException e) {
            // ignore
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            // ignore
        }
    }

}
