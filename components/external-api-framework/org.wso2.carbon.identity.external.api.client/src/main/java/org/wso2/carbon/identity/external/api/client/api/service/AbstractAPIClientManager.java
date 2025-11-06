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
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientResponseException;
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

    protected AbstractAPIClientManager(APIClientConfig apiClientConfig) {

        this.apiClient = new APIClient(apiClientConfig);
    }

    public APIResponse callAPI(APIRequestContext requestContext, APIInvocationConfig apiInvocationConfig)
            throws APIClientException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("AbstractAPIClientManager calling API for endpoint: %s",
                    requestContext.getEndpointUrl()));
        }

        APIResponse response = apiClient.callAPI(requestContext, apiInvocationConfig);
        return handleResponse(response);
    }

    protected abstract APIResponse handleResponse(APIResponse response) throws APIClientResponseException;
}
