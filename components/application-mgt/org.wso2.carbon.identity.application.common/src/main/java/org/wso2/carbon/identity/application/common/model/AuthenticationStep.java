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
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AuthenticationStep implements Serializable {

    private static final long serialVersionUID = 497647508006862448L;

    private int stepOrder = 1;
    private LocalAuthenticatorConfig[] localAuthenticatorConfigs = new LocalAuthenticatorConfig[0];
    private IdentityProvider[] federatedIdentityProviders = new IdentityProvider[0];
    private boolean subjectStep;
    private boolean attributeStep;

    /*
     * <AuthenticationStep> <StepOrder></StepOrder>
     * <LocalAuthenticatorConfigs></LocalAuthenticatorConfigs>
     * <FederatedIdentityProviders></FederatedIdentityProviders> <SubjectStep></SubjectStep>
     * <AttributeStep></AttributeStep> </AuthenticationStep>
     */
    public static AuthenticationStep build(OMElement authenticationStepOM) {
        AuthenticationStep authenticationStep = new AuthenticationStep();

        Iterator<?> iter = authenticationStepOM.getChildElements();


        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();
            if ("StepOrder".equals(member.getLocalName())) {
                authenticationStep.setStepOrder(Integer.parseInt(member.getText()));
            } else if ("SubjectStep".equals(member.getLocalName())) {
                if (member.getText() != null && member.getText().trim().length() > 0) {
                    authenticationStep.setSubjectStep(Boolean.parseBoolean(member.getText()));
                }
            } else if ("AttributeStep".equals(member.getLocalName())) {
                if (member.getText() != null && member.getText().trim().length() > 0) {
                    authenticationStep.setAttributeStep(Boolean.parseBoolean(member.getText()));
                }
            } else if ("FederatedIdentityProviders".equals(member.getLocalName())) {

                Iterator<?> federatedIdentityProvidersIter = member.getChildElements();
                List<IdentityProvider> federatedIdentityProvidersArrList = new ArrayList<IdentityProvider>();

                if (federatedIdentityProvidersIter != null) {
                    while (federatedIdentityProvidersIter.hasNext()) {
                        OMElement federatedIdentityProvidersElement = (OMElement) (federatedIdentityProvidersIter
                                .next());
                        IdentityProvider idp = IdentityProvider
                                .build(federatedIdentityProvidersElement);
                        if (idp != null) {
                            federatedIdentityProvidersArrList.add(idp);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(federatedIdentityProvidersArrList)) {
                    IdentityProvider[] federatedAuthenticatorConfigsArr = federatedIdentityProvidersArrList
                            .toArray(new IdentityProvider[0]);
                    authenticationStep
                            .setFederatedIdentityProviders(federatedAuthenticatorConfigsArr);
                }
            } else if ("LocalAuthenticatorConfigs".equals(member.getLocalName())) {

                Iterator<?> localAuthenticatorConfigsIter = member.getChildElements();
                List<LocalAuthenticatorConfig> localAuthenticatorConfigsArrList = new ArrayList<LocalAuthenticatorConfig>();

                if (localAuthenticatorConfigsIter != null) {
                    while (localAuthenticatorConfigsIter.hasNext()) {
                        OMElement localAuthenticatorConfigsElement = (OMElement) (localAuthenticatorConfigsIter
                                .next());
                        LocalAuthenticatorConfig localAuthConfig = LocalAuthenticatorConfig
                                .build(localAuthenticatorConfigsElement);
                        if (localAuthConfig != null) {
                            localAuthenticatorConfigsArrList.add(localAuthConfig);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(localAuthenticatorConfigsArrList)) {
                    LocalAuthenticatorConfig[] localAuthenticatorConfigsArr = localAuthenticatorConfigsArrList
                            .toArray(new LocalAuthenticatorConfig[0]);
                    authenticationStep.setLocalAuthenticatorConfigs(localAuthenticatorConfigsArr);
                }
            }
        }
        return authenticationStep;
    }

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
