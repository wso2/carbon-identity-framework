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
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
    private static final String ORGANIZATION_HANDLE = "organizationHandle";
    private static final Log LOG = LogFactory.getLog(OrganizationDataRetrievalClient.class);

    /**
     * Check whether the given organization handle is available by invoking the Organization Management API.
     * The tenant is taken from the current request context so the portal credentials are always used against the
     * tenant that the request belongs to.
     *
     * @param jsonBody The request body containing the organization handle.
     * @return OrganizationCheckResponse containing the status code and response body from the API.
     * @throws ApiException If there is an error during API invocation or if input validation fails.
     */
    public OrganizationCheckResponse checkHandle(String jsonBody) throws ApiException {

        String organizationHandle = extractOrganizationHandle(jsonBody);
        String tenantDomain = resolveTenantDomain();
        String authorizationHeader = buildClientAuthorizationHeader();

        return HTTPClientManager.executeWithHttpClient(httpClient -> {
            try {
                String path = IdentityManagementEndpointUtil.getBasePath(tenantDomain, CHECK_HANDLE_API_PATH,
                        true, false);
                HttpPost postRequest = new HttpPost(path);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
                JSONObject requestBody = new JSONObject();
                requestBody.put(ORGANIZATION_HANDLE, organizationHandle);
                postRequest.setEntity(new StringEntity(requestBody.toString(),
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
                try {
                    return new OrganizationCheckResponse(statusCode, new JSONObject(content));
                } catch (JSONException e) {
                    // The API can return a non-JSON error page. Keep the real status code so the caller can
                    // react to it instead of reporting a generic server error.
                    return new OrganizationCheckResponse(statusCode, new JSONObject());
                }
            }
        });
    }

    private String extractOrganizationHandle(String jsonBody) throws ApiException {

        if (StringUtils.isBlank(jsonBody)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing the request body when checking the organization handle.");
        }

        try {
            Object organizationHandle = new JSONObject(jsonBody).opt(ORGANIZATION_HANDLE);
            if (!(organizationHandle instanceof String) || StringUtils.isBlank((String) organizationHandle)) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                        "Missing the required parameter 'organizationHandle' when checking the organization handle.");
            }
            return (String) organizationHandle;
        } catch (JSONException e) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request body when checking the organization handle.");
        }
    }

    /**
     * Resolve the tenant domain of the current request so the portal credentials are not used against a tenant
     * chosen by the caller.
     *
     * @return Tenant domain of the current request context.
     * @throws ApiException If the tenant domain is not available in the current context.
     */
    private String resolveTenantDomain() throws ApiException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isBlank(tenantDomain)) {
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Missing the tenant domain in the current context.");
        }
        return tenantDomain;
    }

    /**
     * Build the client authentication header used to invoke the check-handle API.
     *
     * @return Authorization header value in the form "Client base64(appName:appPassword)".
     * @throws ApiException If the client authentication credentials are not configured.
     */
    private String buildClientAuthorizationHeader() throws ApiException {

        IdentityManagementServiceUtil serviceUtil = IdentityManagementServiceUtil.getInstance();
        String appName = serviceUtil.getAppName();
        char[] appPassword = serviceUtil.getAppPassword();
        if (StringUtils.isBlank(appName) || appPassword == null) {
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Client authentication credentials are not configured.");
        }

        String clientCredentials = appName + ":" + String.valueOf(appPassword);
        return CLIENT_AUTH_TYPE + " " + Base64.getEncoder()
                .encodeToString(clientCredentials.getBytes(StandardCharsets.UTF_8));
    }
}
