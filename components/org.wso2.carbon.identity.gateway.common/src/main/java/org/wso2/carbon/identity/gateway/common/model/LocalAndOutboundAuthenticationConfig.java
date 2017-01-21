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