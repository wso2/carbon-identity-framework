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

package org.wso2.carbon.identity.external.api.token.handler.api.exception;

import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;

/**
 * Exception class for Token Request related exceptions.
 */
public class TokenRequestException extends TokenHandlerException {

    /**
     * Constructor with error message and description data.
     *
     * @param errorMessage    Error message enum.
     * @param descriptionData Description data for the error.
    */
    public TokenRequestException(ErrorMessage errorMessage, String descriptionData) {

        super(errorMessage, descriptionData);
    }

    /**
     * Constructor with error message, description data and cause.
     *
     * @param errorMessage    Error message enum.
     * @param descriptionData Description data for the error.
     * @param cause           Throwable cause.
     */
    public TokenRequestException(ErrorMessage errorMessage, String descriptionData, Throwable cause) {

        super(errorMessage, descriptionData, cause);
    }
}
