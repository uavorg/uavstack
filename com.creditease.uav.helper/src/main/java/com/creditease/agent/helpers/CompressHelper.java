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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.creditease.agent.helpers.compress.CustomClassLoaderObjectInputStream;

public class CompressHelper {

    public static String compress(String str) {

        if (null == str || str.length() <= 0) {
            return str;
        }
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 使用默认缓冲区大小创建新的输出流
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            // 将 b.length 个字节写入此输出流
            gzip.write(str.getBytes());
            gzip.close();
            // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
            return out.toString("ISO-8859-1");
        }
        catch (IOException e) {
            // ignore
        }
        return null;

    }

    /**
     * 字符串的解压
     * 
     * @param str
     *            对字符串解压
     * @return 返回解压缩后的字符串
     * @throws IOException
     */
    public static String unCompress(String str) {

        try {
            if (null == str || str.length() <= 0) {
                return str;
            }
            // 创建一个新的 byte 数组输出流
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
            ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
            // 使用默认缓冲区大小创建新的输入流
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
                // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
                out.write(buffer, 0, n);
            }
            // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
            return out.toString("UTF-8");
        }

        catch (IOException e) {
            // ignore
        }
        return null;
    }

    /**
     * Compress a byte array with GZIP
     * 
     * @param byteArr
     *            a byte array
     * @return a compressed byte array
     * @throws IOException
     */
    public static byte[] compressByteArrWithGZIP(byte[] byteArr) throws IOException {

        ByteArrayOutputStream bos = null;
        GZIPOutputStream gos = null;
        try {
            bos = new ByteArrayOutputStream();
            gos = new GZIPOutputStream(bos);
            gos.write(byteArr);
            gos.flush();
        }
        finally {
            closeStream(gos);
            closeStream(bos);
        }

        return bos.toByteArray();
    }

    /**
     * Serialize one object to byte array
     * 
     * @param object
     *            an object of a class which should implement Serializable interface
     * @return one byte array
     * @throws IOException
     */
    public static byte[] transObjectToByteArr(Serializable object) throws IOException {

        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
        }
        finally {
            closeStream(oos);
            closeStream(bos);
        }

        return bos.toByteArray();
    }

    /**
     * Uncompress a byte array with GZIP
     * 
     * @param byteArr
     *            a compressed byte array
     * @return a uncompressed byte array
     * @throws IOException
     */
    public static byte[] uncompressByteArrWithGZIP(byte[] byteArr) throws IOException {

        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        GZIPInputStream gis = null;

        try {
            bos = new ByteArrayOutputStream();
            bis = new ByteArrayInputStream(byteArr);
            gis = new GZIPInputStream(bis);

            byte[] buffer = new byte[1024];
            int count = 0;

            while ((count = gis.read(buffer)) >= 0) {
                bos.write(buffer, 0, count);
            }
        }
        finally {
            closeStream(bos);
            closeStream(bis);
            closeStream(gis);
        }

        return bos.toByteArray();
    }

    /**
     * Deserialize a byte array to one object
     * 
     * @param byteArr
     * @return one object of a class which implements Serializable interface
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Serializable readObjectFromByteArr(byte[] byteArr, ClassLoader classloader)
            throws IOException, ClassNotFoundException {

        ByteArrayInputStream bis = null;

        ObjectInputStream ois = null;

        try {
            bis = new ByteArrayInputStream(byteArr);

            if (classloader != null) {
                ois = new CustomClassLoaderObjectInputStream(bis, classloader);
            }
            else {
                ois = new ObjectInputStream(bis);
            }
        }
        finally {
            closeStream(bis);
            closeStream(ois);
        }

        return (Serializable) ois.readObject();
    }

    /**
     * Check if a byte array is compressed by GZIP
     * 
     * @param byteArr
     *            a byte array
     * @return true stands for compressed data; false stands for uncompressed data
     * @throws IOException
     */
    public static boolean isCompressedByGZIP(byte[] byteArr) throws IOException {

        ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
        int b = readUByte(bis);
        int num = readUByte(bis) << 8 | b;
        return num == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Read one byte from a InputStream
     * 
     * @param in
     *            one InputStream
     * @return The integer value of the byte
     * @throws IOException
     */
    private static int readUByte(InputStream in) throws IOException {

        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            // Report on this.in, not argument in; see read{Header, Trailer}.
            throw new IOException(in.getClass().getName() + ".read() returned value out of range -1..255: " + b);
        }
        return b;
    }

    /**
     * Close the stream which implements Closeable interface
     * 
     * @param stream
     */
    private static void closeStream(Closeable stream) {

        if (null != stream) {
            try {
                stream.close();
            }
            catch (IOException e) {
                stream = null;
                e.printStackTrace();
            }
        }
    }
}
