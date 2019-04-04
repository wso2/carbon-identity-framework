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
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "LocalAndOutboundAuthenticationConfig")
public class LocalAndOutboundAuthenticationConfig implements Serializable {

    private static final long serialVersionUID = 6552125621314155291L;
    private static final String USE_USERSTORE_DOMAIN_IN_USERNAME = "UseUserstoreDomainInUsername";
    private static final String USE_TENANT_DOMAIN_IN_USERNAME = "UseTenantDomainInUsername";
    private static final String USE_USERSTORE_DOMAIN_IN_ROLES = "UseUserstoreDomainInRoles";
    private static final String ENABLE_AUTHORIZATION = "EnableAuthorization";
    private static final String SUBJECT_CLAIM_URI = "subjectClaimUri";
    private static final String ALWAYS_SEND_BACK_AUTHENTICATED_LIST_OF_ID_PS = "alwaysSendBackAuthenticatedListOfIdPs";
    private static final String AUTHENTICATION_STEP_FOR_ATTRIBUTES = "AuthenticationStepForAttributes";
    private static final String AUTHENTICATION_STEP_FOR_SUBJECT = "AuthenticationStepForSubject";
    private static final String AUTHENTICATION_STEPS = "AuthenticationSteps";
    private static final String AUTHENTICATION_GRAPH = "AuthenticationGraph";
    private static final String AUTHENTICATION_SCRIPT = "AuthenticationScript";

    @XmlElementWrapper(name = "AuthenticationSteps")
    @XmlElement(name = "AuthenticationStep")
    private AuthenticationStep[] authenticationSteps = new AuthenticationStep[0];

    @XmlElement(name = "AuthenticationType")
    private String authenticationType;

    @XmlElement(name = AUTHENTICATION_STEP_FOR_SUBJECT)
    private AuthenticationStep authenticationStepForSubject;

    @XmlElement(name = AUTHENTICATION_STEP_FOR_ATTRIBUTES)
    private AuthenticationStep authenticationStepForAttributes;

    @XmlElement(name = ALWAYS_SEND_BACK_AUTHENTICATED_LIST_OF_ID_PS)
    private boolean alwaysSendBackAuthenticatedListOfIdPs;

    @XmlElement(name = SUBJECT_CLAIM_URI)
    private String subjectClaimUri;

    @XmlElement(name = USE_TENANT_DOMAIN_IN_USERNAME)
    private boolean useTenantDomainInLocalSubjectIdentifier = false;

    @XmlElement(name = USE_USERSTORE_DOMAIN_IN_ROLES)
    private boolean useUserstoreDomainInRoles = false;

    @XmlElement(name = USE_USERSTORE_DOMAIN_IN_USERNAME)
    private boolean useUserstoreDomainInLocalSubjectIdentifier = false;

    @XmlElement(name = ENABLE_AUTHORIZATION)
    private boolean enableAuthorization = false;

    @XmlElement(name = AUTHENTICATION_SCRIPT)
    private AuthenticationScriptConfig authenticationScriptConfig;

    /*
     * <LocalAndOutboundAuthenticationConfig> <AuthenticationSteps></AuthenticationSteps>
     * <AuthenticationType></AuthenticationType>
     * <AuthenticationStepForSubject></AuthenticationStepForSubject>
     * <AuthenticationStepForAttributes></AuthenticationStepForAttributes>
     * <AuthenticationScript></AuthenticationScript>
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

            if (AUTHENTICATION_SCRIPT.equals(member.getLocalName())) {
                localAndOutboundAuthenticationConfig.authenticationScriptConfig = AuthenticationScriptConfig
                        .build(member);
            } else if (AUTHENTICATION_STEPS.equals(member.getLocalName())) {

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
            } else if (USE_USERSTORE_DOMAIN_IN_ROLES.equals(member.getLocalName())) {
                if (Boolean.parseBoolean(member.getText())) {
                    localAndOutboundAuthenticationConfig.setUseUserstoreDomainInRoles(true);
                }
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

    public AuthenticationScriptConfig getAuthenticationScriptConfig(){return authenticationScriptConfig;}

    public void setAuthenticationScriptConfig(AuthenticationScriptConfig authenticationScriptConfig) {
        this.authenticationScriptConfig = authenticationScriptConfig;
    }

    public boolean isUseUserstoreDomainInRoles() {

        return useUserstoreDomainInRoles;
    }

    public void setUseUserstoreDomainInRoles(boolean useUserstoreDomainInRoles) {

        this.useUserstoreDomainInRoles = useUserstoreDomainInRoles;
    }
}
