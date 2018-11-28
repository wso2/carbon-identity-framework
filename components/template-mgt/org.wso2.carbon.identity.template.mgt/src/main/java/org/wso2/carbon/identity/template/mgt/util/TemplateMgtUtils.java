/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.template.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementSQLException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementServerException;

/**
 * This class is used to define the Utilities required in template management feature.
 */
public class TemplateMgtUtils {

    /**
     * This method can be used to generate a TemplateManagementServerException from
     * IdentityTemplateMgtConstants.ErrorMessages.
     * object when no exception is thrown.
     *
     * @param error IdentityTemplateMgtConstants.ErrorMessages.
     * @param data  data to replace if the message needs to be replaced.
     * @return TemplateManagementServerException.
     */
    public static TemplateManagementServerException handleServerException(TemplateMgtConstants.ErrorMessages error,
                                                                          String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return new TemplateManagementServerException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a TemplateManagementClientException from TemplateMgtConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error templateMgtConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return TemplateManagementClientException.
     */
    public static TemplateManagementClientException handleClientException(TemplateMgtConstants.ErrorMessages error,
                                                                          String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new TemplateManagementClientException(message, error.getCode());
    }

    /**
     * This method can be used to generate a TemplateManagementSQLException from TemplateMgtConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error templateMgtConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return TemplateManagementSQLException.
     */
    public static TemplateManagementSQLException handleSQLException(TemplateMgtConstants.ErrorMessages error,
                                                                       String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new TemplateManagementSQLException(message, error.getCode(), e);
    }

    /**
     * Get the Tenant Id from carbon context.
     *
     * @return Tenant Id.
     */
    public static int getTenantIdFromCarbonContext() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

}


