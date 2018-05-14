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

import java.io.Serializable;
import java.util.Iterator;

public class JustInTimeProvisioningConfig extends InboundProvisioningConfig implements Serializable {

    private static final long serialVersionUID = 6754801699494009980L;

    private boolean passwordProvisioningEnabled;
    private String userStoreClaimUri;
    private boolean modifyUserNameAllowed;

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
            } else if ("IsPasswordProvisioningEnabled".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    justInTimeProvisioningConfig
                            .setPasswordProvisioningEnabled(Boolean.parseBoolean(element.getText()));
                }
            } else if ("AllowModifyUserName".equals(elementName)) {
                if (element.getText() != null && element.getText().trim().length() > 0) {
                    justInTimeProvisioningConfig.setModifyUserNameAllowed(Boolean.parseBoolean(element.getText()));
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
}
