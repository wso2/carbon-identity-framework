/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.identity.external.api.client.api.exception.APIClientConfigException;
import org.wso2.carbon.identity.external.api.client.internal.util.APIClientUtils;

import static org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage.ERROR_CODE_INVALID_RESPONSE_LIMIT;
import static org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage.ERROR_CODE_INVALID_RETRY_COUNT;

/**
 * Model class for API request Configuration for API invocation.
 */
public class APIInvocationConfig {

    private int allowedRetryCount = APIClientUtils.getDefaultRetryCount();

    /**
     * Per-request response size limit in bytes.
     * When null, the limit defined in {@link org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig}
     * is used as the effective limit.
     */
    private Long responseLimitInBytes = null;

    /**
     * Get the allowed retry count.
     *
     * @return allowed retry count.
     */
    public int getAllowedRetryCount() {

        return allowedRetryCount;
    }

    /**
     * Set the allowed retry count.
     *
     * @param allowedRetryCount allowed retry count.
     * @throws APIClientConfigException if the retry count is negative.
     */
    public void setAllowedRetryCount(int allowedRetryCount) throws APIClientConfigException {

        if (allowedRetryCount < 0) {
            throw new APIClientConfigException(ERROR_CODE_INVALID_RETRY_COUNT, String.valueOf(allowedRetryCount));
        }
        this.allowedRetryCount = allowedRetryCount;
    }

    /**
     * Get the per-request response size limit in bytes, or null if no override is set for this invocation
     * (meaning the client-level limit from
     * {@link org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig} will be used).
     *
     * @return response size limit in bytes, or null to fallback to the client-level default.
     */
    public Long getResponseLimitInBytes() {

        return responseLimitInBytes;
    }

    /**
     * Override the response size limit for this specific invocation.
     * Set this to override the client-level limit defined in
     * {@link org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig}.
     *
     * @param responseLimitInBytes response size limit in bytes; must be a positive long.
     * @throws APIClientConfigException if the supplied value is not positive.
     */
    public void setResponseLimitInBytes(long responseLimitInBytes) throws APIClientConfigException {

        if (responseLimitInBytes <= 0) {
            throw new APIClientConfigException(ERROR_CODE_INVALID_RESPONSE_LIMIT,
                    String.valueOf(responseLimitInBytes));
        }
        this.responseLimitInBytes = responseLimitInBytes;
    }
}
