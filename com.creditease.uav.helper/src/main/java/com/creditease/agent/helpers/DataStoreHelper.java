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

/**
 * 
 * @author peihua
 * 
 */
public class DataStoreHelper {

    public static String decodeForMongoDB(String s) {

        return s.replace("/u2e", ".").replace("/u24", "$");
    }

    public static String decorateInForMongoDB(String src) {

        StringBuilder sb = new StringBuilder(src.length());
        char[] chars = src.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '.' || chars[i] == '$') {
                sb.append("/u" + Integer.toHexString(chars[i]));
            }
            else if (chars[i] == '#') {
                sb.append(".");
            }
            else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    public static String encodeForMongoDB(String s) {

        StringBuilder sb = new StringBuilder(s.length());
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (chars[i] == '.' || chars[i] == '$') {
                sb.append("/u" + Integer.toHexString(c));
            }
            else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    public static String encodeForOpenTSDB(String s) {

        s = s.replace(":", "/u003a").replace("%", "/u0025").replace("#", "/u0023").replace("+", "/u002B")
                .replace(";", "/u003b").replace("=", "/u003d").replace("!", "/u0021").replace("@", "/u0040")
                .replace("$", "/u0024").replace("^", "/u005e").replace("&", "/u0026").replace("*", "/u002a")
                .replace("(", "/u0028").replace(")", "/u0029")// .replace("_", "/u005f").replace("-", "/u002d")
                .replace("{", "/u007b").replace("}", "/u007d").replace("[", "/u005b").replace("]", "/u005d")
                .replace("\\", "/u005c").replace("|", "/u007c").replace("\"", "/u0022").replace("'", "/u0027")
                .replace("<", "/u003c").replace(",", "/u002c").replace(">", "/u003e").replace("?", "/u003f");

        return s;
    }

    public static String decodeForOpenTSDB(String s) {

        s = s.replace("/u003a", ":").replace("/u0025", "%").replace("/u0023", "#").replace("/u002B", "+")
                .replace("/u003b", ";").replace("/u003d", "=").replace("/u0021", "!").replace("/u0040", "@")
                .replace("/u0024", "$").replace("/u005e", "^").replace("/u0026", "&").replace("/u002a", "*")
                .replace("/u0028", "(").replace("/u0029", ")")// .replace("/u005f", "_").replace("/u002d", "-")
                .replace("/u007b", "{").replace("/u007d", "}").replace("/u005b", "[").replace("/u005d", "]")
                .replace("/u005c", "\\").replace("/u007c", "|").replace("/u0022", "\"").replace("/u0027", "'")
                .replace("/u003c", "<").replace("/u002c", ",").replace("/u003e", ">").replace("/u003f", "?");
        return s;
    }
}
