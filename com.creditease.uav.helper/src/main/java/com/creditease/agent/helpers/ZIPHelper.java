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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZIPHelper {

    /***
     * Compress files or directories in the source path, support compress multiple directories at the same time
     * 
     * @param sources
     *            The array of absolute source path to be compressed
     * @param zipFile
     *            target zip file's name
     * @throws IOException
     */
    public static void compressFilesToZip(String[] sources, String zipFile) throws IOException {

        compressFilesToZip("", sources, zipFile);
    }

    /**
     * Compress files or directories in the source path
     * 
     * @param entryHead
     *            the head of every zip entry, it can be empty
     * @param sources
     *            source path array to be compressed
     * @param zipFile
     *            target zip file's name
     * @throws IOException
     */
    public static void compressFilesToZip(String entryHead, String[] sources, String zipFile) throws IOException {

        List<File> fileList = new LinkedList<File>();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(new File(zipFile));
            zos = new ZipOutputStream(fos);
            for (String source : sources) {
                if (Files.notExists((Paths.get(source)))) {
                    continue;
                }

                fileList = loadFilename(new File(source));
                byte[] byteArr = new byte[8192];
                int length = -1;
                BufferedInputStream bis = null;
                for (int i = 0; i < fileList.size(); i++) {
                    File file = fileList.get(i);
                    zos.putNextEntry(new ZipEntry(getEntryName(entryHead, source, file)));
                    bis = new BufferedInputStream(new FileInputStream(file));

                    while ((length = bis.read(byteArr)) != -1) {
                        zos.write(byteArr, 0, length);
                    }
                    bis.close();
                    zos.closeEntry();
                }
            }
        }
        finally {
            if (zos != null) {
                zos.close();
                zos = null;
            }
        }
    }

    /**
     * Use recursion to get all the files from source path
     * 
     * @param file
     *            source path
     * @return a list of file
     */
    private static List<File> loadFilename(File file) {

        List<File> fileList = new ArrayList<File>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                fileList.addAll(loadFilename(f));
            }
        }
        else {
            fileList.add(file);
        }

        return fileList;
    }

    /**
     * 
     * @param entryHead
     *            the head of every zip entry, it can be empty
     * @param source
     * @param file
     * @return
     * @throws IOException
     */
    private static String getEntryName(String entryHead, String source, File file) throws IOException {

        File baseFile = new File(source);
        String filename = file.getPath();
        String entryName = null;

        if (baseFile.getParentFile().getParentFile() == null) {
            entryName = filename.substring(baseFile.getParent().length());
        }
        else {
            entryName = filename.substring(baseFile.getParent().length() + 1);
        }

        return StringHelper.isEmpty(entryHead) ? entryName : entryHead + File.separator + entryName;
    }

    /**
     * Decompress one zip file to target directory
     * 
     * @param zipFile
     * @param targetDir
     * @throws IOException
     */
    public static void decompressZipToDir(String zipFile, String targetDir) throws IOException {

        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> en = zip.entries();
            ZipEntry entry = null;
            byte[] buffer = new byte[8192];
            int length = -1;
            InputStream input = null;
            BufferedOutputStream bos = null;
            File file = null;

            while (en.hasMoreElements()) {
                entry = en.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                input = zip.getInputStream(entry);
                file = new File(targetDir, entry.getName());
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                bos = new BufferedOutputStream(new FileOutputStream(file));

                while (true) {
                    length = input.read(buffer);
                    if (length == -1)
                        break;
                    bos.write(buffer, 0, length);
                }
                bos.close();
                input.close();
            }
        }
        finally {
            if (zip != null) {
                zip.close();
                zip = null;
            }
        }
    }

    public static void main(String[] args) throws IOException {

        compressFilesToZip(new String[] { "D://test//1", "D://test//2", "D://test//1.txt" }, "D://test//test.zip");
        decompressZipToDir("D://test//test.zip", "D://test");
    }
}
