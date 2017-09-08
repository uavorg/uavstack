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

import java.io.IOException;
import java.io.OutputStream;

// Referenced classes of package sun.misc:
// CharacterEncoder

public class BASE64EncoderUrl extends CharacterEncoderUrl {

    public BASE64EncoderUrl() {
    }

    @Override
    protected int bytesPerAtom() {

        return 3;
    }

    @Override
    protected int bytesPerLine() {

        return 57;
    }

    @Override
    protected void encodeAtom(OutputStream outputstream, byte abyte0[], int i, int j) throws IOException {

        if (j == 1) {
            byte byte0 = abyte0[i];
            int k = 0;
            outputstream.write(pem_array[byte0 >>> 2 & 63]);
            outputstream.write(pem_array[(byte0 << 4 & 48) + (k >>> 4 & 15)]);
            outputstream.write(61);
            outputstream.write(61);
            // outputstream.write(42); //*
            // outputstream.write(42);
        }
        else if (j == 2) {
            byte byte1 = abyte0[i];
            byte byte3 = abyte0[i + 1];
            int l = 0;
            outputstream.write(pem_array[byte1 >>> 2 & 63]);
            outputstream.write(pem_array[(byte1 << 4 & 48) + (byte3 >>> 4 & 15)]);
            outputstream.write(pem_array[(byte3 << 2 & 60) + (l >>> 6 & 3)]);
            outputstream.write(61);
            // outputstream.write(42);
        }
        else {
            byte byte2 = abyte0[i];
            byte byte4 = abyte0[i + 1];
            byte byte5 = abyte0[i + 2];
            outputstream.write(pem_array[byte2 >>> 2 & 63]);
            outputstream.write(pem_array[(byte2 << 4 & 48) + (byte4 >>> 4 & 15)]);
            outputstream.write(pem_array[(byte4 << 2 & 60) + (byte5 >>> 6 & 3)]);
            outputstream.write(pem_array[byte5 & 63]);
        }
    }

    private static final char pem_array[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '-', '_' };

}

/*
 * DECOMPILATION REPORT
 * 
 * Decompiled from: D:\UAVIDE\jdk7\jre\lib\rt.jar Total time: 46 ms Jad reported messages/errors: Exit status: 0 Caught
 * exceptions:
 */