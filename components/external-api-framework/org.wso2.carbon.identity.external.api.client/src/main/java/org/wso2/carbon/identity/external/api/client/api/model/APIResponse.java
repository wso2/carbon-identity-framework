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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Model class for API Response.
 */
public class APIResponse {

    private static final Log LOG = LogFactory.getLog(APIResponse.class);

    private final int statusCode;
    private final String responseBody;

    public APIResponse(int statusCode, String responseBody) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating APIResponse with status code: %d, response body length: %d",
                    statusCode, responseBody != null ? responseBody.length() : 0));
        }

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Get the status code of the API response.
     *
     * @return status code.
     */
    public int getStatusCode() {

        return statusCode;
    }

    /**
     * Get the response body of the API response.
     *
     * @return response body.
     */
    public String getResponseBody() {

        return responseBody;
    }
}
