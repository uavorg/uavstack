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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * JavaScript interpreter built-in JDK
 *
 */
public class JsHelper {

    private static ScriptEngine jsEngine;

    static {
        ScriptEngineManager sem = new ScriptEngineManager();
        jsEngine = sem.getEngineByName("js");
    }

    private JsHelper() {
    }

    public static Object eval(String script) {

        if (StringHelper.isEmpty(script)) {
            return null;
        }

        try {
            return jsEngine.eval(script);
        }
        catch (ScriptException e) {
            // TODO just return null
            return null;
        }
    }
}
