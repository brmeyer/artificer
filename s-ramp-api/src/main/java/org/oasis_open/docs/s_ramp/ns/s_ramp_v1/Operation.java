/*
 * Copyright 2014 JBoss Inc
 *
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
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.18 at 01:49:11 PM EST 
//


package org.oasis_open.docs.s_ramp.ns.s_ramp_v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Operation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Operation">
 *   &lt;complexContent>
 *     &lt;extension base="{http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0}NamedWsdlDerivedArtifactType">
 *       &lt;sequence>
 *         &lt;element name="input" type="{http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0}operationInputTarget"/>
 *         &lt;element name="output" type="{http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0}operationOutputTarget"/>
 *         &lt;element name="fault" type="{http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0}faultTarget" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Operation", propOrder = {
    "input",
    "output",
    "fault"
})
public class Operation
    extends NamedWsdlDerivedArtifactType
    implements Serializable
{

    private static final long serialVersionUID = -7582834828345836591L;
    @XmlElement(required = true)
    protected OperationInputTarget input;
    @XmlElement(required = true)
    protected OperationOutputTarget output;
    protected List<FaultTarget> fault;

    /**
     * Gets the value of the input property.
     * 
     * @return
     *     possible object is
     *     {@link OperationInputTarget }
     *     
     */
    public OperationInputTarget getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationInputTarget }
     *     
     */
    public void setInput(OperationInputTarget value) {
        this.input = value;
    }

    /**
     * Gets the value of the output property.
     * 
     * @return
     *     possible object is
     *     {@link OperationOutputTarget }
     *     
     */
    public OperationOutputTarget getOutput() {
        return output;
    }

    /**
     * Sets the value of the output property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationOutputTarget }
     *     
     */
    public void setOutput(OperationOutputTarget value) {
        this.output = value;
    }

    /**
     * Gets the value of the fault property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fault property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFault().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FaultTarget }
     * 
     * 
     */
    public List<FaultTarget> getFault() {
        if (fault == null) {
            fault = new ArrayList<FaultTarget>();
        }
        return this.fault;
    }

}
