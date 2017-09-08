package com.creditease.monitorframework.fat.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * 
 * <pre>
 * &lt;complexType name="echoFaultResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "echoFaultResponse", propOrder = {
        "_return" }, namespace = "http://client.fat.monitorframework.creditease.com/")
public class EchoFaultResponse {

    @XmlElement(name = "return")
    protected String _return;

    /**
     *
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getReturn() {

        return _return;
    }

    /**
     * 
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setReturn(String value) {

        this._return = value;
    }

}
