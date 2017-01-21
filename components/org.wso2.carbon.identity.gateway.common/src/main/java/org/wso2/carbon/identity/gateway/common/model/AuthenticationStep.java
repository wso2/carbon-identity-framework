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

package org.wso2.carbon.identity.gateway.common.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthenticationStep implements Serializable {

    private static final long serialVersionUID = 497647508006862448L;

    private int stepOrder = 1;
    private LocalAuthenticatorConfig[] localAuthenticatorConfigs = new LocalAuthenticatorConfig[0];
    private IdentityProvider[] federatedIdentityProviders = new IdentityProvider[0];
    private boolean subjectStep;
    private boolean attributeStep;


    /**
     * @return
     */
    public int getStepOrder() {
        return stepOrder;
    }

    /**
     * @param stepOrder
     */
    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    /**
     * @return
     */
    public LocalAuthenticatorConfig[] getLocalAuthenticatorConfigs() {
        return localAuthenticatorConfigs;
    }

    /**
     * @param localAuthenticatorConfigs
     */
    public void setLocalAuthenticatorConfigs(LocalAuthenticatorConfig[] localAuthenticatorConfigs) {
        if (localAuthenticatorConfigs == null) {
            return;
        }
        Set<LocalAuthenticatorConfig> propertySet =
                new HashSet<LocalAuthenticatorConfig>(Arrays.asList(localAuthenticatorConfigs));
        this.localAuthenticatorConfigs = propertySet.toArray(new LocalAuthenticatorConfig[propertySet.size()]);
    }

    /**
     * @return
     */
    public IdentityProvider[] getFederatedIdentityProviders() {
        return federatedIdentityProviders;
    }

    /**
     * @param federatedIdentityProviders
     */
    public void setFederatedIdentityProviders(IdentityProvider[] federatedIdentityProviders) {
        if (federatedIdentityProviders == null) {
            return;
        }
        Set<IdentityProvider> propertySet = new HashSet<>(Arrays.asList(federatedIdentityProviders));
        this.federatedIdentityProviders = propertySet.toArray(new IdentityProvider[propertySet.size()]);
    }

    /**
     * @return
     */
    public boolean isSubjectStep() {
        return subjectStep;
    }

    /**
     * @param subjectStep
     */
    public void setSubjectStep(boolean subjectStep) {
        this.subjectStep = subjectStep;
    }

    /**
     * @return
     */
    public boolean isAttributeStep() {
        return attributeStep;
    }

    /**
     * @param attributeStep
     */
    public void setAttributeStep(boolean attributeStep) {
        this.attributeStep = attributeStep;
    }
}
