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

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * EncryptionHelper description: JAVA 加密工具类
 * 
 * 在线测试工具：http://tool.chacuo.net/cryptaes
 */
public class EncryptionHelper {

    /**
     * 
     * AES CBC加密
     * 
     * 数据块：128位
     * 
     * @param strMsg
     *            ： 需要加密的字符串
     * @param sKey
     *            :加密key(16位)
     * @param sKeyFormat
     *            :skey是否格式处理
     * @param ivParameter
     *            :偏移量(16位)使用CBC模式，需要一个向量iv，可增加加密算法的强度
     * @param encoding
     *            :编码
     * @return ：16进制的加密字符串
     */
    public static String encryptByAesCBC(String strMsg, String sKey, boolean sKeyFormat, String ivParameter,
            String encoding) {

        try {
            byte[] keyByte = null;
            if (sKeyFormat) {
                KeyGenerator kgen = KeyGenerator.getInstance("AES");
                kgen.init(128, new SecureRandom(sKey.getBytes()));
                SecretKey secretKey = kgen.generateKey();
                keyByte = secretKey.getEncoded();

            }
            else {
                keyByte = sKey.getBytes(encoding);
            }

            SecretKeySpec skeySpec = new SecretKeySpec(keyByte, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 算法/模式/补码方式
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] resultByte = cipher.doFinal(strMsg.getBytes(encoding));

            // 传输过程,转成16进制
            String resultStr = parseByte2HexStr(resultByte);
            return resultStr;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 
     * AES CBC解密
     * 
     * 数据块：128位
     * 
     * @param hexadecimalMsg
     *            :16进制的加密字符串
     * @param sKey
     *            :加密key(16位)
     * @param sKeyFormat
     *            :skey是否格式处理
     * @param ivParameter
     *            :偏移量(16位)使用CBC模式，需要一个向量iv，可增加加密算法的强度
     * @param encoding
     *            :编码
     * @return :解密后字符串
     */
    public static String decryptByAesCBC(String hexadecimalMsg, String sKey, boolean sKeyFormat, String ivParameter,
            String encoding) {

        try {

            byte[] keyByte = null;
            if (sKeyFormat) {
                KeyGenerator kgen = KeyGenerator.getInstance("AES");
                kgen.init(128, new SecureRandom(sKey.getBytes()));
                SecretKey secretKey = kgen.generateKey();
                keyByte = secretKey.getEncoded();
            }
            else {
                keyByte = sKey.getBytes(encoding);
            }

            SecretKeySpec skeySpec = new SecretKeySpec(keyByte, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 算法/模式/补码方式
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] msgByte = parseHexStr2Byte(hexadecimalMsg);
            byte[] resultByte = cipher.doFinal(msgByte);

            return new String(resultByte, encoding);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 将二进制转换成16进制
     * 
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     * 
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {

        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        String sKey = "UavappHubaEs_keY";
        boolean sKeyFormat = false;
        String ivParameter = "UavappHubaEs_keY";
        String encoding = "utf-8";

        // 需要加密的字串
        String str = "uavtestmsg测试信息:123456";
        System.out.println("\r\n加密前的字串:\r\n" + str);
        // 加密
        String encryptStr = encryptByAesCBC(str, sKey, sKeyFormat, ivParameter, encoding);
        System.out.println("\r\n加密后的字串:\r\n" + encryptStr);

        // 解密
        System.out.println("\r\n解密后的字串:\r\n" + decryptByAesCBC(encryptStr, sKey, sKeyFormat, ivParameter, encoding));
    }

}
