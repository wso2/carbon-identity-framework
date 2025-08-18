/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a mapping between local and remote attributes for account lookup.
 * This is used in the context of just-in-time provisioning of federated user
 * login.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AccountLookupAttributeMappingConfig")
public class AccountLookupAttributeMappingConfig implements Serializable {

    private static final long serialVersionUID = -8123917549203847111L;

    private static final String LOCAL_ATTRIBUTE = "localAttribute";
    private static final String FEDERATED_ATTRIBUTE = "federatedAttribute";
    @XmlElement(name = LOCAL_ATTRIBUTE)
    private String localAttribute = null;
    @XmlElement(name = FEDERATED_ATTRIBUTE)
    private String federatedAttribute = null;

    /**
     * <AccountLookupAttributeMapping>
     *     <localAttribute></localAttribute>
     *     <remoteAttribute></remoteAttribute>
     * </AccountLookupAttributeMapping>
     */
    public static AccountLookupAttributeMappingConfig build(OMElement accountLookupAttributeMappingOM) {

        AccountLookupAttributeMappingConfig
                accountLookupAttributeMappingConfig = new AccountLookupAttributeMappingConfig();
        if (accountLookupAttributeMappingOM == null) {
            return accountLookupAttributeMappingConfig;
        }
        Iterator<?> iterator = accountLookupAttributeMappingOM.getChildElements();
        while (iterator.hasNext()) {
            OMElement element = (OMElement) iterator.next();
            String elementName = element.getLocalName();
            if (LOCAL_ATTRIBUTE.equals(elementName)) {
                accountLookupAttributeMappingConfig.setLocalAttribute(element.getText());
            } else if (FEDERATED_ATTRIBUTE.equals(elementName)) {
                accountLookupAttributeMappingConfig.setFederatedAttribute(element.getText());
            }
        }
        return accountLookupAttributeMappingConfig;
    }

    /**
     * Default constructor to create an empty AccountLookupAttributeMappingConfig.
     */
    public AccountLookupAttributeMappingConfig() {

    }

    /**
     * Constructor to create an AccountLookupAttributeMappingConfig with local and
     * federated attribute names.
     *
     * @param localAttribute      Local attribute name.
     * @param federatedAttribute  Federated attribute name.
     */
    public AccountLookupAttributeMappingConfig(String localAttribute, String federatedAttribute) {

        this.localAttribute = localAttribute;
        this.federatedAttribute = federatedAttribute;
    }

    /**
     * Get the local attribute name.
     *
     * @return Local attribute name.
     */
    public String getLocalAttribute() {

        return localAttribute;
    }

    /**
     * Set the local attribute name.
     *
     * @param localAttribute Local attribute name.
     */
    public void setLocalAttribute(String localAttribute) {

        this.localAttribute = localAttribute;
    }

    /**
     * Get the remote attribute name.
     *
     * @return Remote attribute name.
     */
    public String getFederatedAttribute() {

        return federatedAttribute;
    }

    /**
     * Set the remote attribute name.
     *
     * @param federatedAttribute Remote attribute name.
     */
    public void setFederatedAttribute(String federatedAttribute) {

        this.federatedAttribute = federatedAttribute;
    }
}
