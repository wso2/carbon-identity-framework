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

package org.wso2.carbon.identity.core.context.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.internal.context.OrganizationResolver;

import java.io.IOException;

import javax.servlet.ServletException;

public class IdentityContextCreatorValve extends ValveBase {

    private static final Log LOG = LogFactory.getLog(IdentityContextCreatorValve.class);
    private static final String TOKEN_PATH = "/oauth2/token";
    private static final String URL_SEPARATOR = "/";
    private static final String TENANT_SEPARATOR = "t";
    private static final String ORG_SEPARATOR = "o";

    public IdentityContextCreatorValve() {
        // Enable async support to handle asynchronous requests, allowing non-blocking operations
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        try {
            initIdentityContext();
            initRequest(request);
            initAccessTokenIssuedOrganization(request.getRequestURI());
            OrganizationResolver.getInstance().resolveOrganizationInContext(request);
            getNext().invoke(request, response);
        } catch (Exception e) {
            LOG.error("Could not handle request: " + request.getRequestURI(), e);
        } finally {
            // This will destroy the identity context data holder on the current thread.
            IdentityContext.destroyCurrentContext();
        }
    }

    private void initAccessTokenIssuedOrganization(String requestURI) {

        if (StringUtils.isBlank(requestURI)) {
            return;
        }

        requestURI = requestURI.trim().toLowerCase();
        if (!requestURI.endsWith(TOKEN_PATH)) {
            return;
        }
        /*
         * Possible structures:
         * 1. t/<tenant>/oauth2/token                    => issuer = root
         * 2. t/<tenant>/o/<org>/oauth2/token           => issuer = root
         * 3. o/<org>/oauth2/token                      => issuer = sub-org
         */
        if (requestURI.startsWith(URL_SEPARATOR)) {
            requestURI = requestURI.substring(1);
        }

        String[] parts = requestURI.split(URL_SEPARATOR);
        if (parts.length < 3) {
            return;
        }

        String accessTokenIssuedOrganization;
        if (TENANT_SEPARATOR.equals(parts[0])) {
            // Case 1 & 2: /t/<tenant>/oauth2/token
            // Case 3: /t/<tenant>/o/<org>/oauth2/token (virtual org)
            accessTokenIssuedOrganization = parts[1];
        } else if (ORG_SEPARATOR.equals(parts[0])) {
            // Case 4: /o/<org>/oauth2/token
            accessTokenIssuedOrganization = parts[1];
        } else {
            return;
        }
        IdentityContext.getThreadLocalIdentityContext().setAccessTokenIssuedOrganization(accessTokenIssuedOrganization);
    }

    public void initIdentityContext() {

        IdentityContext.getThreadLocalIdentityContext();
    }

    private void initRequest(Request request) {

        if (request == null) {
            LOG.debug("Http Request is null. Skipping request initialization in IdentityContext.");
            return;
        }

        org.wso2.carbon.identity.core.context.model.Request requestToSetInContext =
                new org.wso2.carbon.identity.core.context.model.Request.Builder().fromHttpRequest(request).build();
        IdentityContext.getThreadLocalIdentityContext().setRequest(requestToSetInContext);
    }
}
