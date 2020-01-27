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
 */
package org.wso2.carbon.identity.functions.library.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementClientException;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementServerException;

/**
 * Function library management error handling class.
 */
public class FunctionLibraryExceptionManagementUtil {

    /**
     * This method can be used to generate a FunctionLibraryManagementClientException from
     * FunctionLibraryManagementConstants.ErrorMessage object when no exception is thrown.
     *
     * @param error FunctionLibraryManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return FunctionLibraryManagementClientException.
     */
    public static FunctionLibraryManagementClientException handleClientException(
            FunctionLibraryManagementConstants.ErrorMessage error, String data) {

        String message = includeData(error, data);
        return new FunctionLibraryManagementClientException(error.getCode(), message);
    }

    public static FunctionLibraryManagementClientException handleClientException(
            FunctionLibraryManagementConstants.ErrorMessage error, String data, Throwable e) {

        String message = includeData(error, data);
        return new FunctionLibraryManagementClientException(error.getCode(), message, e);
    }

    public static FunctionLibraryManagementClientException handleClientException(
            FunctionLibraryManagementConstants.ErrorMessage error) {

        String message = error.getMessage();
        return new FunctionLibraryManagementClientException(error.getCode(), message);
    }

    /**
     * This method can be used to generate a FunctionLibraryManagementServerException from
     * FunctionLibraryManagementConstants.ErrorMessage object when no exception is thrown.
     *
     * @param error FunctionLibraryManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return FunctionLibraryManagementServerException.
     */
    public static FunctionLibraryManagementServerException handleServerException(
            FunctionLibraryManagementConstants.ErrorMessage error, String data, Throwable e) {

        String message = includeData(error, data);
        return new FunctionLibraryManagementServerException(error.getCode(), message, e);
    }

    public static FunctionLibraryManagementServerException handleServerException(
            FunctionLibraryManagementConstants.ErrorMessage error) {

        String message = error.getMessage();
        return new FunctionLibraryManagementServerException(error.getCode(), message);
    }

    public static FunctionLibraryManagementServerException handleServerException(
            FunctionLibraryManagementConstants.ErrorMessage error, Throwable e) {

        String message = error.getMessage();
        return new FunctionLibraryManagementServerException(error.getCode(), message, e);
    }

    /**
     * Include the data to the error message.
     *
     * @param error FunctionLibraryManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return message format with data.
     */
    private static String includeData(FunctionLibraryManagementConstants.ErrorMessage error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }

}
