/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.HTTPClientManager;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.organization.v1.OrganizationCheckResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletResponse;

/**
 * Client to check the availability of an organization handle.
 */
public class OrganizationDataRetrievalClient {

    private static final String CHECK_HANDLE_API_PATH = "/api/server/v1/organizations/check-handle";
    private static final String CLIENT_AUTH_TYPE = "Client";
    private static final Log LOG = LogFactory.getLog(OrganizationDataRetrievalClient.class);

    /**
     * Check whether the given organization handle is available by invoking the Organization Management API.
     *
     * @param jsonBody     The JSON body to be sent in the request.
     * @param tenantDomain The tenant domain for which the handle availability is checked.
     * @return OrganizationCheckResponse containing the status code and response body from the API.
     * @throws ApiException If there is an error during API invocation or if input validation fails.
     */
    public OrganizationCheckResponse checkHandle(String jsonBody, String tenantDomain) throws ApiException {

        validateInputs(jsonBody, tenantDomain);

        return HTTPClientManager.executeWithHttpClient(httpClient -> {
            try {
                String path = IdentityManagementEndpointUtil.getBasePath(tenantDomain, CHECK_HANDLE_API_PATH,
                        true, false);
                HttpPost postRequest = new HttpPost(path);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, buildClientAuthorizationHeader());
                postRequest.setEntity(new StringEntity(jsonBody,
                        ContentType.create(HTTPConstants.MEDIA_TYPE_APPLICATION_JSON, StandardCharsets.UTF_8)));
                return execute(httpClient, postRequest);
            } catch (JSONException | IOException e) {
                LOG.error("Error while invoking organization handle check request.", e);
                throw new ApiException("Error while invoking organization handle check request.");
            }
        });
    }

    private OrganizationCheckResponse execute(CloseableHttpClient httpClient, HttpUriRequestBase request)
            throws IOException {

        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        return httpClient.execute(request, response -> {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();

            if (entity == null) {
                return new OrganizationCheckResponse(statusCode, new JSONObject());
            }

            try (InputStream is = entity.getContent()) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                if (StringUtils.isBlank(content)) {
                    return new OrganizationCheckResponse(statusCode, new JSONObject());
                }
                return new OrganizationCheckResponse(statusCode, new JSONObject(content));
            }
        });
    }

    /**
     * Build the client authentication header used to invoke the check-handle API.
     *
     * @return Authorization header value in the form "Client base64(appName:appPassword)".
     */
    private String buildClientAuthorizationHeader() {

        IdentityManagementServiceUtil serviceUtil = IdentityManagementServiceUtil.getInstance();
        String clientCredentials = serviceUtil.getAppName() + ":" + String.valueOf(serviceUtil.getAppPassword());
        return CLIENT_AUTH_TYPE + " " + Base64.getEncoder()
                .encodeToString(clientCredentials.getBytes(StandardCharsets.UTF_8));
    }

    private void validateInputs(String jsonBody, String tenantDomain) throws ApiException {

        if (StringUtils.isBlank(jsonBody)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing the request body when checking the organization handle.");
        }

        validateTenantDomain(tenantDomain, "checking the organization handle");
    }

    private void validateTenantDomain(String tenantDomain, String operation) throws ApiException {

        if (StringUtils.isBlank(tenantDomain)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing the required parameter 'tenantDomain' when " + operation + ".");
        }
    }
}
