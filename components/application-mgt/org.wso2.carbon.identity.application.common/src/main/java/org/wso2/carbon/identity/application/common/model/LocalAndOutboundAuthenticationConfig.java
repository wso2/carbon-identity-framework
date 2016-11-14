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
import java.util.Iterator;
import java.util.List;

public class LocalAndOutboundAuthenticationConfig implements Serializable {

    private static final long serialVersionUID = 6552125621314155291L;
    private static final String USE_USERSTORE_DOMAIN_IN_USERNAME = "UseUserstoreDomainInUsername";
    private static final String USE_TENANT_DOMAIN_IN_USERNAME = "UseTenantDomainInUsername";
    private static final String ENABLE_AUTHORIZATION = "EnableAuthorization";
    private static final String SUBJECT_CLAIM_URI = "subjectClaimUri";
    private static final String ALWAYS_SEND_BACK_AUTHENTICATED_LIST_OF_ID_PS = "alwaysSendBackAuthenticatedListOfIdPs";
    private static final String AUTHENTICATION_STEP_FOR_ATTRIBUTES = "AuthenticationStepForAttributes";
    private static final String AUTHENTICATION_STEP_FOR_SUBJECT = "AuthenticationStepForSubject";
    private static final String AUTHENTICATION_STEPS = "AuthenticationSteps";

    private AuthenticationStep[] authenticationSteps = new AuthenticationStep[0];
    private String authenticationType;
    private AuthenticationStep authenticationStepForSubject;
    private AuthenticationStep authenticationStepForAttributes;
    private boolean alwaysSendBackAuthenticatedListOfIdPs;
    private String subjectClaimUri;
    private boolean useTenantDomainInLocalSubjectIdentifier = false;
    private boolean useUserstoreDomainInLocalSubjectIdentifier = false;
    private boolean enableAuthorization = false;

    /*
     * <LocalAndOutboundAuthenticationConfig> <AuthenticationSteps></AuthenticationSteps>
     * <AuthenticationType></AuthenticationType>
     * <AuthenticationStepForSubject></AuthenticationStepForSubject>
     * <AuthenticationStepForAttributes></AuthenticationStepForAttributes>
     * </LocalAndOutboundAuthenticationConfig>
     */
    public static LocalAndOutboundAuthenticationConfig build(
            OMElement localAndOutboundAuthenticationConfigOM) {

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();

        if (localAndOutboundAuthenticationConfigOM == null) {
            return localAndOutboundAuthenticationConfig;
        }

        Iterator<?> iter = localAndOutboundAuthenticationConfigOM.getChildElements();

        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();

            if (AUTHENTICATION_STEPS.equals(member.getLocalName())) {

                Iterator<?> authenticationStepsIter = member.getChildElements();
                List<AuthenticationStep> authenticationStepsArrList = new ArrayList<AuthenticationStep>();

                if (authenticationStepsIter != null) {
                    while (authenticationStepsIter.hasNext()) {
                        OMElement authenticationStepsElement = (OMElement) (authenticationStepsIter
                                .next());
                        AuthenticationStep authStep = AuthenticationStep
                                .build(authenticationStepsElement);
                        if (authStep != null) {
                            authenticationStepsArrList.add(authStep);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(authenticationStepsArrList)) {
                    AuthenticationStep[] authenticationStepsArr = authenticationStepsArrList
                            .toArray(new AuthenticationStep[0]);
                    localAndOutboundAuthenticationConfig
                            .setAuthenticationSteps(authenticationStepsArr);
                }


            } else if ("AuthenticationType".equals(member.getLocalName())) {
                localAndOutboundAuthenticationConfig.setAuthenticationType(member.getText());
            } else if (AUTHENTICATION_STEP_FOR_SUBJECT.equals(member.getLocalName())) {
                AuthenticationStep authStep = AuthenticationStep.build(member);
                if (authStep != null) {
                    localAndOutboundAuthenticationConfig.setAuthenticationStepForSubject(authStep);
                }
            } else if (AUTHENTICATION_STEP_FOR_ATTRIBUTES.equals(member.getLocalName())) {
                AuthenticationStep authStep = AuthenticationStep.build(member);
                if (authStep != null) {
                    localAndOutboundAuthenticationConfig
                            .setAuthenticationStepForAttributes(authStep);
                }
            } else if (ALWAYS_SEND_BACK_AUTHENTICATED_LIST_OF_ID_PS.equals(member.getLocalName())) {
                if (member.getText() != null && "true".equals(member.getText())) {
                    localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(true);
                }
            } else if (USE_USERSTORE_DOMAIN_IN_USERNAME.equals(member.getLocalName())) {
                if (Boolean.parseBoolean(member.getText())) {
                    localAndOutboundAuthenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(true);
                }
            } else if (USE_TENANT_DOMAIN_IN_USERNAME.equals(member.getLocalName())) {
                if (Boolean.parseBoolean(member.getText())) {
                    localAndOutboundAuthenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(true);
                }
            } else if (ENABLE_AUTHORIZATION.equals(member.getLocalName())) {
                if (Boolean.parseBoolean(member.getText())) {
                    localAndOutboundAuthenticationConfig.setEnableAuthorization(true);
                }
            } else if (SUBJECT_CLAIM_URI.equals(member.getLocalName())) {
                localAndOutboundAuthenticationConfig.setSubjectClaimUri(member.getText());
            }
        }

        return localAndOutboundAuthenticationConfig;
    }

    /**
     * @return
     */
    public AuthenticationStep[] getAuthenticationSteps() {
        return authenticationSteps;
    }

    /**
     * @param authenticationSteps
     */
    public void setAuthenticationSteps(AuthenticationStep[] authenticationSteps) {
        this.authenticationSteps = authenticationSteps;
    }

    /**
     * @return
     */
    public String getAuthenticationType() {
        return authenticationType;
    }

    /**
     * @param authenticationType
     */
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
     * @return
     */
    public AuthenticationStep getAuthenticationStepForSubject() {
        return authenticationStepForSubject;
    }

    /**
     * @param authenticationStepForSubject
     */
    public void setAuthenticationStepForSubject(AuthenticationStep authenticationStepForSubject) {
        this.authenticationStepForSubject = authenticationStepForSubject;
    }

    /**
     * @return
     */
    public AuthenticationStep getAuthenticationStepForAttributes() {
        return authenticationStepForAttributes;
    }

    /**
     * @param authenticationStepForAttributes
     */
    public void setAuthenticationStepForAttributes(
            AuthenticationStep authenticationStepForAttributes) {
        this.authenticationStepForAttributes = authenticationStepForAttributes;
    }

    /**
     * @return
     */
    public boolean isAlwaysSendBackAuthenticatedListOfIdPs() {
        return alwaysSendBackAuthenticatedListOfIdPs;
    }

    /**
     * @param alwaysSendBackAuthenticatedListOfIdPs
     */
    public void setAlwaysSendBackAuthenticatedListOfIdPs(boolean alwaysSendBackAuthenticatedListOfIdPs) {
        this.alwaysSendBackAuthenticatedListOfIdPs = alwaysSendBackAuthenticatedListOfIdPs;
    }

    /**
     * @return
     */
    public String getSubjectClaimUri() {
        return subjectClaimUri;
    }

    /**
     * @param subjectClaimUri
     */
    public void setSubjectClaimUri(String subjectClaimUri) {
        this.subjectClaimUri = subjectClaimUri;
    }

    public boolean isUseTenantDomainInLocalSubjectIdentifier() {
        return useTenantDomainInLocalSubjectIdentifier;
    }

    public void setUseTenantDomainInLocalSubjectIdentifier(boolean useTenantDomainInLocalSubjectIdentifier) {
        this.useTenantDomainInLocalSubjectIdentifier = useTenantDomainInLocalSubjectIdentifier;
    }

    public boolean isUseUserstoreDomainInLocalSubjectIdentifier() {
        return useUserstoreDomainInLocalSubjectIdentifier;
    }

    public void setUseUserstoreDomainInLocalSubjectIdentifier(boolean useUserstoreDomainInLocalSubjectIdentifier) {
        this.useUserstoreDomainInLocalSubjectIdentifier = useUserstoreDomainInLocalSubjectIdentifier;
    }

    public boolean isEnableAuthorization() {

        return enableAuthorization;
    }

    public void setEnableAuthorization(boolean enableAuthorization) {

        this.enableAuthorization = enableAuthorization;
    }
}