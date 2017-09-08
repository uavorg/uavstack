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

package com.creditease.uav.helpers.webhttp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * 请求输出流包装
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.0
 */
public class RequestOutputStream extends BufferedOutputStream {

    final CharsetEncoder encoder;

    /**
     * Create request output stream
     *
     * @param stream
     * @param charset
     * @param bufferSize
     */
    public RequestOutputStream(final OutputStream stream, final String charset, final int bufferSize) {
        super(stream, bufferSize);

        encoder = Charset.forName(HttpRequest.getValidCharset(charset)).newEncoder();
    }

    /**
     * Write string to stream
     *
     * @param value
     * @return this stream
     * @throws IOException
     */
    public RequestOutputStream write(final String value) throws IOException {

        final ByteBuffer bytes = encoder.encode(CharBuffer.wrap(value));

        super.write(bytes.array(), 0, bytes.limit());

        return this;
    }
}
