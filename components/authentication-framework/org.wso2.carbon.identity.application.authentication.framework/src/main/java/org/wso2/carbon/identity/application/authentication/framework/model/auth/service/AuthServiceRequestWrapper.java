/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.model.auth.service;

import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A HttpServletRequest wrapper to be used in authentication service.
 */
public class AuthServiceRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String[]> parameters = new HashMap<>();
    private boolean isAuthFlowConcluded;

    public AuthServiceRequestWrapper(HttpServletRequest request, Map<String, String[]> parameters) {

        super(request);
        this.parameters = parameters;
        setSessionDataKey(parameters);
        skipNonceCookieValidation();
        this.setAttribute(FrameworkConstants.IS_API_BASED_AUTH_FLOW, true);
    }

    @Override
    public String getParameter(String name) {

        if (this.parameters.containsKey(name) && this.parameters.get(name).length > 0) {
            return this.parameters.get(name)[0];
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {

        Map<String, String[]> paramMap = new HashMap<>(super.getParameterMap());
        paramMap.putAll(this.parameters);

        return paramMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {

        Set<String> paramNames = new HashSet<>(this.parameters.keySet());
        paramNames.addAll(Collections.list(super.getParameterNames()));

        return Collections.enumeration(paramNames);
    }

    @Override
    public String[] getParameterValues(String name) {

        if (this.parameters.containsKey(name)) {
            return this.parameters.get(name);
        }

        return super.getParameterValues(name);
    }

    /**
     * Get the authentication initiation data related to
     * any authenticators engaged in the request flow.
     *
     * @return List of {@link AuthenticatorData} objects.
     */
    public List<AuthenticatorData> getAuthInitiationData() {

        List<AuthenticatorData> authenticatorData =
                (List<AuthenticatorData>) getAttribute(AuthServiceConstants.AUTH_SERVICE_AUTH_INITIATION_DATA);
        return authenticatorData != null ? authenticatorData : Collections.emptyList();
    }

    /**
     * Check whether the request is a multi options response.
     * This check is done by checking the presence of the
     * attribute {@link FrameworkConstants#IS_MULTI_OPS_RESPONSE}
     * from the request.
     *
     * @return true if the attribute is present, false otherwise.
     */
    public boolean isMultiOptionsResponse() {

        return Boolean.TRUE.equals(getAttribute(FrameworkConstants.IS_MULTI_OPS_RESPONSE));
    }

    /**
     * Get the authenticator flow status.
     * This check is done by checking the presence of the
     * attribute {@link FrameworkConstants.RequestParams#FLOW_STATUS}
     * from the request.
     *
     * @return {@link AuthenticatorFlowStatus} if the attribute is present, null otherwise.
     */
    public AuthenticatorFlowStatus getAuthFlowStatus() {

        return (AuthenticatorFlowStatus) getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);
    }

    /**
     * Check whether the complete authentication flow is concluded.
     *
     * @return true if the flow is concluded, false otherwise.
     */
    public boolean isAuthFlowConcluded() {

        return Boolean.TRUE.equals(getAttribute(FrameworkConstants.IS_AUTH_FLOW_CONCLUDED)) || isAuthFlowConcluded;
    }

    /**
     * Mark whether the flow is concluded.
     *
     * @param isAuthFlowConcluded set true if the flow is concluded.
     */
    public void setAuthFlowConcluded(boolean isAuthFlowConcluded) {

        this.isAuthFlowConcluded = isAuthFlowConcluded;
    }

    private void setSessionDataKey(Map<String, String[]> parameters) {

        if (parameters.containsKey(AuthServiceConstants.FLOW_ID)) {
            this.parameters.put(FrameworkConstants.SESSION_DATA_KEY, parameters.get(AuthServiceConstants.FLOW_ID));
        }
    }

    private void skipNonceCookieValidation() {

        this.setAttribute(FrameworkConstants.SKIP_NONCE_COOKIE_VALIDATION, true);
    }

    /**
     * Get the session data key.
     *
     * @return String of session data key.
     */
    public String getSessionDataKey() {

        if (this.parameters.containsKey(FrameworkConstants.SESSION_DATA_KEY)) {
            String[] sessionDataKeyParam = this.parameters.get(FrameworkConstants.SESSION_DATA_KEY);
            if (sessionDataKeyParam != null && sessionDataKeyParam.length > 0) {
                return sessionDataKeyParam[0];
            }
        }

        Object contextIdentifierAttr = getAttribute(FrameworkConstants.CONTEXT_IDENTIFIER);
        if (contextIdentifierAttr != null) {
            return contextIdentifierAttr.toString();
        }

        return null;
    }

    /**
     * Check if the request was sent to retry.
     *
     * @return True if sent to retry.
     */
    public boolean isSentToRetry() {

        return Boolean.TRUE.equals(getAttribute(FrameworkConstants.IS_SENT_TO_RETRY));
    }
}
