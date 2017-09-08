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

package com.creditease.monitorframework.fat.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * com.creditease.monitorframework.fat.service package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EchoFaultResponse_QNAME = new QName(
            "http://service.fat.monitorframework.creditease.com/", "echoFaultResponse");
    private final static QName _EchoFault_QNAME = new QName("http://service.fat.monitorframework.creditease.com/",
            "echoFault");
    private final static QName _Echo_QNAME = new QName("http://service.fat.monitorframework.creditease.com/", "echo");
    private final static QName _EchoResponse_QNAME = new QName("http://service.fat.monitorframework.creditease.com/",
            "echoResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * com.creditease.monitorframework.fat.service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link com.creditease.monitorframework.fat.client.EchoFault }
     * 
     */
    public EchoFault createEchoFault() {

        return new EchoFault();
    }

    /**
     * Create an instance of {@link com.creditease.monitorframework.fat.client.EchoFaultResponse }
     * 
     */
    public EchoFaultResponse createEchoFaultResponse() {

        return new EchoFaultResponse();
    }

    /**
     * Create an instance of {@link com.creditease.monitorframework.fat.client.EchoResponse }
     * 
     */
    public EchoResponse createEchoResponse() {

        return new EchoResponse();
    }

    /**
     * Create an instance of {@link com.creditease.monitorframework.fat.client.Echo }
     * 
     */
    public Echo createEcho() {

        return new Echo();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}
     * {@link com.creditease.monitorframework.fat.client.EchoFaultResponse }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://service.fat.monitorframework.creditease.com/", name = "echoFaultResponse")
    public JAXBElement<EchoFaultResponse> createEchoFaultResponse(EchoFaultResponse value) {

        return new JAXBElement<EchoFaultResponse>(_EchoFaultResponse_QNAME, EchoFaultResponse.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}
     * {@link com.creditease.monitorframework.fat.client.EchoFault } {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://service.fat.monitorframework.creditease.com/", name = "echoFault")
    public JAXBElement<EchoFault> createEchoFault(EchoFault value) {

        return new JAXBElement<EchoFault>(_EchoFault_QNAME, EchoFault.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}
     * {@link com.creditease.monitorframework.fat.client.Echo }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://service.fat.monitorframework.creditease.com/", name = "echo")
    public JAXBElement<Echo> createEcho(Echo value) {

        return new JAXBElement<Echo>(_Echo_QNAME, Echo.class, null, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}
     * {@link com.creditease.monitorframework.fat.client.EchoResponse } {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://service.fat.monitorframework.creditease.com/", name = "echoResponse")
    public JAXBElement<EchoResponse> createEchoResponse(EchoResponse value) {

        return new JAXBElement<EchoResponse>(_EchoResponse_QNAME, EchoResponse.class, null, value);
    }

}
