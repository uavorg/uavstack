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

package com.creditease.uav.helpers.url;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

public abstract class CharacterDecoderUrl {

    public CharacterDecoderUrl() {
    }

    protected abstract int bytesPerAtom();

    protected abstract int bytesPerLine();

    protected void decodeBufferPrefix(PushbackInputStream pushbackinputstream, OutputStream outputstream)
            throws IOException {

    }

    protected void decodeBufferSuffix(PushbackInputStream pushbackinputstream, OutputStream outputstream)
            throws IOException {

    }

    protected int decodeLinePrefix(PushbackInputStream pushbackinputstream, OutputStream outputstream)
            throws IOException {

        return bytesPerLine();
    }

    protected void decodeLineSuffix(PushbackInputStream pushbackinputstream, OutputStream outputstream)
            throws IOException {

    }

    protected void decodeAtom(PushbackInputStream pushbackinputstream, OutputStream outputstream, int i)
            throws IOException {

        throw new IOException();
    }

    protected int readFully(InputStream inputstream, byte abyte0[], int i, int j) throws IOException {

        for (int k = 0; k < j; k++) {
            int l = inputstream.read();
            if (l == -1)
                return k != 0 ? k : -1;
            abyte0[k + i] = (byte) l;
        }

        return j;
    }

    public void decodeBuffer(InputStream inputstream, OutputStream outputstream) throws IOException {

        @SuppressWarnings("unused")
        int j = 0;
        PushbackInputStream pushbackinputstream = new PushbackInputStream(inputstream);
        decodeBufferPrefix(pushbackinputstream, outputstream);
        try {
            do {
                int k = decodeLinePrefix(pushbackinputstream, outputstream);
                int i;
                for (i = 0; i + bytesPerAtom() < k; i += bytesPerAtom()) {
                    decodeAtom(pushbackinputstream, outputstream, bytesPerAtom());
                    j += bytesPerAtom();
                }

                if (i + bytesPerAtom() == k) {
                    decodeAtom(pushbackinputstream, outputstream, bytesPerAtom());
                    j += bytesPerAtom();
                }
                else {
                    decodeAtom(pushbackinputstream, outputstream, k - i);
                    j += k - i;
                }
                decodeLineSuffix(pushbackinputstream, outputstream);
            }
            while (true);
        }
        catch (IOException cestreamexhausted) {
            decodeBufferSuffix(pushbackinputstream, outputstream);
        }
    }

    @SuppressWarnings("deprecation")
    public byte[] decodeBuffer(String s) throws IOException {

        byte abyte0[] = new byte[s.length()];
        s.getBytes(0, s.length(), abyte0, 0);
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        decodeBuffer(((bytearrayinputstream)), ((bytearrayoutputstream)));
        return bytearrayoutputstream.toByteArray();
    }

    public byte[] decodeBuffer(InputStream inputstream) throws IOException {

        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        decodeBuffer(inputstream, ((bytearrayoutputstream)));
        return bytearrayoutputstream.toByteArray();
    }

    public ByteBuffer decodeBufferToByteBuffer(String s) throws IOException {

        return ByteBuffer.wrap(decodeBuffer(s));
    }

    public ByteBuffer decodeBufferToByteBuffer(InputStream inputstream) throws IOException {

        return ByteBuffer.wrap(decodeBuffer(inputstream));
    }
}

/*
 * DECOMPILATION REPORT
 * 
 * Decompiled from: D:\UAVIDE\jdk7\jre\lib\rt.jar Total time: 20 ms Jad reported messages/errors: Exit status: 0 Caught
 * exceptions:
 */
