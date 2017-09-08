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

package com.creditease.agent.helpers.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class CustomClassLoaderObjectInputStream extends ObjectInputStream {

    private ClassLoader classLoader;

    protected CustomClassLoaderObjectInputStream() throws IOException, SecurityException {
        super();
    }

    public CustomClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException, IOException {

        try {
            String name = desc.getName();
            return Class.forName(name, false, classLoader);
        }
        catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}
