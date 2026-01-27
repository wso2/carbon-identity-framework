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

package org.wso2.carbon.identity.external.api.client.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.client.internal.service.APIClient;

/**
 * Abstract class for API Client Manager implementations which responsible for handling API calls and responses.
 */
public abstract class AbstractAPIClientManager {

    private static final Log LOG = LogFactory.getLog(AbstractAPIClientManager.class);

    private final APIClient apiClient;

    /**
     * Constructor to initialize the API Client with the provided configuration.
     *
     * @param apiClientConfig API Client Configuration.
     */
    protected AbstractAPIClientManager(APIClientConfig apiClientConfig) {

        this.apiClient = new APIClient(apiClientConfig);
    }

    /**
     * Call the API using the provided request context and invocation config.
     *
     * @param requestContext      API Request Context.
     * @param apiInvocationConfig API Invocation Config.
     * @return API Response.
     * @throws APIClientException           If an error occurs while calling the API.
     */
    public APIResponse callAPI(APIRequestContext requestContext, APIInvocationConfig apiInvocationConfig)
            throws APIClientException {

        if (requestContext == null || apiInvocationConfig == null) {
            throw new IllegalArgumentException("Request context and invocation config cannot be null");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("AbstractAPIClientManager calling API for endpoint: %s",
                    requestContext.getEndpointUrl()));
        }

        return apiClient.callAPI(requestContext, apiInvocationConfig);
    }
}
