/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This holds the response to client side, with the error messages to be shown in UI.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ClientResponse")
public class ClientResponse implements Serializable {

    @XmlElement(name = "ResponseCode")
    private int responseCode;

    @XmlElement(name = "Errors")
    private String[] errors;

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    protected Property[] properties = new Property[0];

    /**
     * Get client side error messages.
     *
     * @return error messages
     */
    public String[] getErrors() {

        return errors;
    }

    /**
     * Set client side error messages.
     *
     * @param errors error messages
     */
    public void setErrors(String[] errors) {

        this.errors = errors;
    }

    /**
     * Get response code.
     *
     * @return response code
     */
    public int getResponseCode() {

        return responseCode;
    }

    /**
     * Set response code.
     *
     * @param responseCode response code
     */
    public void setResponseCode(int responseCode) {

        this.responseCode = responseCode;
    }

    /**
     * Get properties needs for client response.
     *
     * @return properties
     */
    public Property[] getProperties() {

        return properties;
    }

    /**
     * Set properties needs for client response.
     *
     * @param properties properties
     */
    public void setProperties(Property[] properties) {

        if (properties == null) {
            return;
        }
        Set<Property> propertySet = new HashSet<Property>(Arrays.asList(properties));
        this.properties = propertySet.toArray(new Property[propertySet.size()]);
    }
}
