/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementRuntimeException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementServerException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;

import java.util.UUID;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.SQLConstants.MAX_QUERY_LENGTH_SQL;

public class ConfigurationUtils {

    /**
     * This method can be used to generate a ConfigurationManagementClientException from
     * ConfigurationConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConfigurationManagementClientException.
     */
    public static ConfigurationManagementClientException handleClientException(ConfigurationConstants.ErrorMessages error,
                                                                               String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new ConfigurationManagementClientException(message, error.getCode());
    }

    public static ConfigurationManagementClientException handleClientException(ConfigurationConstants.ErrorMessages error,
                                                                               String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new ConfigurationManagementClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementServerException from
     * ConfigurationConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConfigurationManagementServerException.
     */
    public static ConfigurationManagementServerException handleServerException(ConfigurationConstants.ErrorMessages error,
                                                                               String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConfigurationManagementServerException(message, error.getCode());
    }

    public static ConfigurationManagementServerException handleServerException(ConfigurationConstants.ErrorMessages error,
                                                                               String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConfigurationManagementServerException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementRuntimeException from
     * ConfigurationConstants.ErrorMessages object when an exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @param e     Parent exception.
     * @return ConsentManagementRuntimeException
     */
    public static ConfigurationManagementRuntimeException handleRuntimeException(ConfigurationConstants.ErrorMessages error,
                                                                                 String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConfigurationManagementRuntimeException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a ConfigurationManagementRuntimeException from ConfigurationConstants
     * .ErrorMessages
     * object when an exception is thrown.
     *
     * @param error ConfigurationConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return ConsentManagementRuntimeException
     */
    public static ConfigurationManagementRuntimeException handleRuntimeException(ConfigurationConstants.ErrorMessages error,
                                                                                 String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new ConfigurationManagementRuntimeException(message, error.getCode());
    }

    public static String generateUniqueID() {

        return UUID.randomUUID().toString();
    }

    public static int getMaximumQueryLength() {

        return StringUtils.isEmpty(MAX_QUERY_LENGTH_SQL) ? 4194304 : Integer.parseInt(MAX_QUERY_LENGTH_SQL);
    }

    public static boolean useCreatedTimeField() {

        return ConfigurationManagerComponentDataHolder.getUseCreatedTime();
    }
}
