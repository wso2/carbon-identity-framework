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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.HTTPClientManager;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.flow.v1.FlowExecutionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

/**
 * Client to retrieve data from Flow Execution API.
 */
public class FlowDataRetrievalClient {

    private static final String FLOW_EXECUTE_API_PATH = "/api/server/v1/flow/execute";
    private static final String FLOW_POLL_API_PATH = "/api/server/v1/flow/poll";
    private static final Log LOG = LogFactory.getLog(FlowDataRetrievalClient.class);

    /**
     * Execute a flow by invoking the Flow Execution API.
     *
     * @param jsonBody     The JSON body to be sent in the request.
     * @param tenantDomain The tenant domain for which the flow is to be executed.
     * @return FlowExecutionResponse containing the status code and response body from the API.
     * @throws ApiException If there is an error during API invocation or if input validation fails.
     */
    public FlowExecutionResponse executeFlow(String jsonBody, String tenantDomain) throws ApiException {

        validateInputs(jsonBody, tenantDomain);

        return HTTPClientManager.executeWithHttpClient(httpClient -> {
            try {
                String path = IdentityManagementEndpointUtil.getBasePath(tenantDomain, FLOW_EXECUTE_API_PATH,
                        true, false);
                HttpPost postRequest = new HttpPost(path);
                postRequest.setEntity(new StringEntity(jsonBody,
                        ContentType.create(HTTPConstants.MEDIA_TYPE_APPLICATION_JSON, StandardCharsets.UTF_8)));
                return execute(httpClient, postRequest);
            } catch (JSONException | IOException e) {
                LOG.error("Error while invoking flow execution request.", e);
                throw new ApiException("Error while invoking flow execution request.");
            }
        });
    }

    /**
     * Get the status of a flow's polling entry by invoking the Flow Execution API.
     *
     * @param pollToken    Poll token of the flow.
     * @param tenantDomain The tenant domain of the flow.
     * @return FlowExecutionResponse containing the status code and response body from the API.
     * @throws ApiException If there is an error during API invocation or if input validation fails.
     */
    public FlowExecutionResponse pollFlow(String pollToken, String tenantDomain) throws ApiException {

        if (StringUtils.isBlank(pollToken)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Missing the poll token when polling flow.");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing the required parameter 'tenantDomain' when polling flow.");
        }

        return HTTPClientManager.executeWithHttpClient(httpClient -> {
            try {
                String path = IdentityManagementEndpointUtil.getBasePath(tenantDomain, FLOW_POLL_API_PATH,
                        true, false);
                HttpGet getRequest = new HttpGet(path + "?token="
                        + URLEncoder.encode(pollToken, StandardCharsets.UTF_8.name()));
                return execute(httpClient, getRequest);
            } catch (JSONException | IOException e) {
                LOG.error("Error while invoking flow poll request.", e);
                throw new ApiException("Error while invoking flow poll request.");
            }
        });
    }

    private FlowExecutionResponse execute(CloseableHttpClient httpClient, HttpUriRequestBase request)
            throws IOException {

        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        return httpClient.execute(request, response -> {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();

            if (entity == null) {
                return new FlowExecutionResponse(statusCode, new JSONObject());
            }

            try (InputStream is = entity.getContent()) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                return new FlowExecutionResponse(statusCode, new JSONObject(content));
            }
        });
    }

    private void validateInputs(String jsonBody, String tenantDomain) throws ApiException {

        if (StringUtils.isBlank(jsonBody)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Missing the request body when executing flow.");
        }

        if (StringUtils.isBlank(tenantDomain)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing the required parameter 'tenantDomain' when executing flow.");
        }
    }
}
