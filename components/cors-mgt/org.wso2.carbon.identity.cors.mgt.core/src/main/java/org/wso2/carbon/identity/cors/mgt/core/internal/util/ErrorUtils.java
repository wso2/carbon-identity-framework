/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.util;

import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;

/**
 * Error utilities.
 */
public class ErrorUtils {

    /**
     * Handle server exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceServerException instance.
     */
    public static CORSManagementServiceServerException handleServerException(ErrorMessages error, String... data) {

        return new CORSManagementServiceServerException(String.format(error.getDescription(), data), error.getCode());
    }

    /**
     * Handle server exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     Original error.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceServerException instance.
     */
    public static CORSManagementServiceServerException handleServerException(ErrorMessages error, Throwable e,
                                                                             String... data) {

        return new CORSManagementServiceServerException(String.format(error.getDescription(), data), error.getCode(),
                e);
    }

    /**
     * Handle client exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceClientException instance.
     */
    public static CORSManagementServiceClientException handleClientException(ErrorMessages error, String... data) {

        return new CORSManagementServiceClientException(String.format(error.getDescription(), data), error.getCode());
    }

    /**
     * Handle client exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     Original error.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceClientException instance.
     */
    public static CORSManagementServiceClientException handleClientException(ErrorMessages error, Throwable e,
                                                                             String... data) {

        return new CORSManagementServiceClientException(String.format(error.getDescription(), data), error.getCode(),
                e);
    }
}
