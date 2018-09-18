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
package com.creditease.uav.hook.jaxws.interceptors;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.frontend.ClientFactoryBean;

import com.creditease.monitor.proxy.spi.JDKProxyInvokeHandler;
import com.creditease.uav.util.JDKProxyInvokeUtil;

/**
 * JaxWsCxfHookIT description
 *
 */
public class JaxWSCxfHookIT extends JaxWSHookIT{

    /**
     * @param appid
     */
    public JaxWSCxfHookIT(String appid) {

        super(appid);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T create(T t,ClientFactoryBean clientFactoryBean) { 
        
        Binding binding = ((BindingProvider) t).getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(this.handler);
        binding.setHandlerChain(handlerChain);
        
        String wsdlLocation = clientFactoryBean.getAddress();
        
        T tProxy = JDKProxyInvokeUtil.newProxyInstance(clientFactoryBean.getServiceClass().getClassLoader(),
                new Class[] { clientFactoryBean.getServiceClass() },
                new JDKProxyInvokeHandler<T>(t, new ClientStubProcessor(wsdlLocation.toString(), this.handler)));
        return tProxy;
    }

}
