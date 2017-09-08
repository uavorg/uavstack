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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageHelper {

    private PackageHelper() {

    }

    public static String[] getHandlerName(String packageDir) {

        List<String> classNames = getClassName(packageDir, false);
        String[] str = new String[classNames.size()];
        if (packageDir != null) {
            for (int i = 0; i < classNames.size(); i++) {
                str[i] = classNames.get(i);
            }
        }
        return str;
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * 
     * @param packageName
     *            包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {

        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     * 
     * @param packageName
     *            包名
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {

        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if ("file".equals(type)) {
                fileNames = getClassNameByFile(url.getPath(), childPackage);
            }
            else if ("jar".equals(type)) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        }
        else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * 
     * @param filePath
     *            文件路径
     * @param className
     *            类名集合
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, boolean childPackage) {

        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), childPackage));
                }
            }
            else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9,
                            childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * 
     * @param jarPath
     *            jar文件路径
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {

        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        try {
            @SuppressWarnings("resource")
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    dealClassFile(myClassName, childPackage, entryName, packagePath);
                }
            }
        }
        catch (Exception e) {
            // ignore
        }
        return myClassName;
    }

    public static void dealClassFile(List<String> myClassName, boolean childPackage, String entryName,
            String packagePath) {

        if (childPackage) {
            if (entryName.startsWith(packagePath)) {
                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                myClassName.add(entryName);
            }
        }
        else {
            int index = entryName.lastIndexOf("/");
            String myPackagePath;
            if (index != -1) {
                myPackagePath = entryName.substring(0, index);
            }
            else {
                myPackagePath = entryName;
            }
            if (myPackagePath.equals(packagePath)) {
                entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                myClassName.add(entryName);
            }
        }
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     * 
     * @param urls
     *            URL集合
     * @param packagePath
     *            包路径
     * @param childPackage
     *            是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {

        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }
}
