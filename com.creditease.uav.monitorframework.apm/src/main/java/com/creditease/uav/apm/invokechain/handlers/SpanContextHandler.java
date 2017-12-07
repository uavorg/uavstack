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

package com.creditease.uav.apm.invokechain.handlers;

import java.util.Map;

import com.creditease.uav.apm.invokechain.span.Span;
import com.creditease.uav.apm.invokechain.spi.InvokeChainCapHandler;
import com.creditease.uav.apm.invokechain.spi.InvokeChainContext;
import com.creditease.uav.apm.invokechain.spi.InvokeChainSpanContext;

/**
 * 
 * ClientEnd Point InvokeChainHandler usage of client request/response Handler
 * 
 * 
 */

public class SpanContextHandler extends InvokeChainCapHandler {

    @Override
    public void preCap(InvokeChainContext context) {

        // do nothing
    }

    /**
     * Finish Span and Logger the Span Metrics
     */
    @Override
    public void doCap(InvokeChainContext context) {

        // do nothing
    }

    public class SpanContext implements InvokeChainSpanContext {

        @Override
        public Map<String, Span> getSpansFromContext() {

            return spanFactory.getSpansFromContext();
        }

        @Override
        public Span getMainSpan() {

            return spanFactory.getSpanFromContext("main");
        }
    }

}
