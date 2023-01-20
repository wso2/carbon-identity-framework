/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;

import java.io.Serializable;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Just in time provisioning configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "JustInTimeProvisioningConfig")
public class JustInTimeProvisioningConfig extends InboundProvisioningConfig implements Serializable {

    private static final long serialVersionUID = 6754801699494009980L;

    @XmlElement(name = "IsPasswordProvisioningEnabled")
    private boolean passwordProvisioningEnabled = false;
    @XmlElement(name = "UserStoreClaimUri")
    private String userStoreClaimUri;
    @XmlElement(name = "AllowModifyUserName")
    private boolean modifyUserNameAllowed = false;
    @XmlElement(name = "PromptConsent")
    private boolean promptConsent = false;
    @XmlElement(name = "EnableAssociateLocalUser")
    private boolean associateLocalUserEnabled = false;

    /*
     * <JustInTimeProvisioningConfig> <UserStoreClaimUri></UserStoreClaimUri>
     * <ProvisioningUserStore></ProvisioningUserStore> <ProvisioningEnabled></ProvisioningEnabled>
     * </JustInTimeProvisioningConfig>
     */
    public static JustInTimeProvisioningConfig build(OMElement justInTimeProvisioningConfigOM) {
        JustInTimeProvisioningConfig justInTimeProvisioningConfig = new JustInTimeProvisioningConfig();

        if (justInTimeProvisioningConfigOM == null) {
            return justInTimeProvisioningConfig;
        }

        Iterator<?> iter = justInTimeProvisioningConfigOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("UserStoreClaimUri".equals(elementName)) {
                justInTimeProvisioningConfig.setUserStoreClaimUri(element.getText());
            } else if ("ProvisioningUserStore".equals(elementName)) {
                justInTimeProvisioningConfig.setProvisioningUserStore(element.getText());
            } else if ("IsProvisioningEnabled".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    justInTimeProvisioningConfig.setProvisioningEnabled(Boolean
                            .parseBoolean(element.getText()));
                }
            } else if (IdentityApplicationConstants.IS_PASSWORD_PROVISIONING_ENABLED_ELEMENT.equals(elementName)) {
                if (StringUtils.isNotEmpty(element.getText())) {
                    justInTimeProvisioningConfig
                            .setPasswordProvisioningEnabled(Boolean.parseBoolean(element.getText()));
                }
            } else if (IdentityApplicationConstants.ALLOW_MODIFY_USERNAME_ELEMENT.equals(elementName)) {
                if (StringUtils.isNotEmpty(element.getText())) {
                    justInTimeProvisioningConfig.setModifyUserNameAllowed(Boolean.parseBoolean(element.getText()));
                }
            } else if (IdentityApplicationConstants.PROMPT_CONSENT_ELEMENT.equals(elementName)) {
                if (StringUtils.isNotEmpty(element.getText())) {
                    justInTimeProvisioningConfig.setPromptConsent(Boolean.parseBoolean(element.getText()));
                }
            }
        }

        return justInTimeProvisioningConfig;
    }

    /**
     * @return
     */
    public String getUserStoreClaimUri() {
        return userStoreClaimUri;
    }

    /**
     * @param userStoreClaimUri
     */
    public void setUserStoreClaimUri(String userStoreClaimUri) {
        this.userStoreClaimUri = userStoreClaimUri;
    }

    /**
     * To set password provisioning is enabled or disabled.
     *
     * @param isPasswordProvisioningEnabled Parameter to specify whether password provisioning is enabled or not.
     */
    public void setPasswordProvisioningEnabled(boolean isPasswordProvisioningEnabled) {
        this.passwordProvisioningEnabled = isPasswordProvisioningEnabled;
    }

    /**
     * To check whether password provisioning is enabled or not.
     *
     * @return true if the password provisioning is enabled, otherwise false.
     */
    public boolean isPasswordProvisioningEnabled() {

        return passwordProvisioningEnabled;
    }

    /**
     * To associate existing local user when JIT user provisioning is enabled.
     *
     * @param associateLocalUserEnabled to specify whether to associate existing local user when JIT user provisioning.
     */
    public void setAssociateLocalUserEnabled(boolean associateLocalUserEnabled) {

        this.associateLocalUserEnabled = associateLocalUserEnabled;
    }

    public boolean isAssociateLocalUserEnabled() {

        return associateLocalUserEnabled;
    }

    /**
     * To check whether change of user name is allowed for the user.
     *
     * @return true if the user name modification is allowed, otherwise returns false.
     */
    public boolean isModifyUserNameAllowed() {
        return modifyUserNameAllowed;
    }

    /**
     * To set whether modification user name is allowed or not.
     *
     * @param isModifyUserNameAllowed Parameter to specify whether modification of user name is allowed or not.
     */
    public void setModifyUserNameAllowed(boolean isModifyUserNameAllowed) {
        this.modifyUserNameAllowed = isModifyUserNameAllowed;
    }

    /**
     * To check whether prompt consent is set to true.
     * @return true if prompt consent is selected otherwise, false.
     */
    public boolean isPromptConsent() {
        return promptConsent;
    }

    /**
     * To set the prompt consent parameter.
     *
     * @param promptConsent parameter to specify whether to prompt consent or not.
     */
    public void setPromptConsent(boolean promptConsent) {
        this.promptConsent = promptConsent;
    }
}
