/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.ai.service.mgt.exceptions;

import org.wso2.carbon.ai.service.mgt.util.AIHttpClientUtil;

/**
 * Client Exception class for AI service.
 */
public class AIClientException extends Exception {

    private String errorCode;
    private AIHttpClientUtil.HttpResponseWrapper loginFlowAIResponse;

    public AIClientException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }

    public AIClientException(AIHttpClientUtil.HttpResponseWrapper httpResponseWrapper,
                                      String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
        this.loginFlowAIResponse = httpResponseWrapper;
    }

    public AIClientException(String message, Throwable cause) {

        super(cause);
    }

    public AIClientException(String message, String errorCode, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public AIHttpClientUtil.HttpResponseWrapper getLoginFlowAIResponse() {

        return loginFlowAIResponse;
    }
}
