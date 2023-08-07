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

import org.wso2.carbon.identity.application.authentication.framework.exception.auth.service.AuthServiceException;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceUtils;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * A HttpServletResponseWrapper wrapper to be used in authentication service.
 */
public class AuthServiceResponseWrapper extends CommonAuthResponseWrapper {

    private static final String LOCATION_HEADER = "location";

    public AuthServiceResponseWrapper(HttpServletResponse response) {

        super(response);
    }

    /**
     * Get the value of the authenticators query param in the redirect URL.
     *
     * @return String of authenticators and related IdPs engaged in the authentication flow.
     * @throws AuthServiceException
     */
    public String getAuthenticators() throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(getRedirectURL());
        return queryParams.get(AuthServiceConstants.AUTHENTICATORS);
    }

    /**
     * Get the sessionDataKey related to the authentication flow.
     *
     * @return String of sessionDataKey.
     * @throws AuthServiceException
     */
    public String getSessionDataKey() throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(getRedirectURL());
        return queryParams.get(FrameworkConstants.SESSION_DATA_KEY);
    }

    /**
     * Check if the response is an error response.
     * This is determined by checking the existence and the value of the
     * query param {@link AuthServiceConstants#AUTH_FAILURE_PARAM}.
     *
     * @return true if the response is an error response.
     * @throws AuthServiceException
     */
    public boolean isErrorResponse() throws AuthServiceException {

        Map<String, String> queryParams = AuthServiceUtils.extractQueryParams(getRedirectURL());
        return Boolean.parseBoolean(queryParams.get(AuthServiceConstants.AUTH_FAILURE_PARAM));
    }

    @Override
    public String getRedirectURL() {

        String redirectUrl = super.getRedirectURL();
        if (redirectUrl == null) {
            redirectUrl = getHeader(LOCATION_HEADER);
        }
        return redirectUrl;
    }
}
