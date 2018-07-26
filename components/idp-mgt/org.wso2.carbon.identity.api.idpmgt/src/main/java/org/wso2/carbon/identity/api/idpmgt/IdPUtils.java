/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.api.idpmgt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

public class IdPUtils {

    private static final Log log = LogFactory.getLog(IdentityProviderManager.class);

    private IdPUtils () {}

    public static String getIDPLocation(String plainId) {

        return IdentityUtil.getServerURL(IdPConstants.IDP_MGT_CONTEXT_PATH + plainId,
                true, false);
    }

    /**
     * This method can be used to generate a ConsentManagementClientException from IdPConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return IDPMgtBridgeServiceClientException.
     */
    public static IDPMgtBridgeServiceClientException handleClientException(IdPConstants.ErrorMessages error,
                                                                           String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Client exception: %s occurred", message));
        }

        return new IDPMgtBridgeServiceClientException(message, error.getCode());
    }

    /**
     * This method can be used to generate a ConsentManagementClientException from IdPConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return IDPMgtBridgeServiceClientException.
     */
    public static IDPMgtBridgeServiceClientException handleClientException(IdPConstants.ErrorMessages error,
                                                                           String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Client exception: %s occurred", message));
        }

        return new IDPMgtBridgeServiceClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a IDPMgtBridgeServiceServerException from IdPConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return IDPMgtBridgeServiceServerException.
     */
    public static IDPMgtBridgeServiceException handleServerException(IdPConstants.ErrorMessages error,
                                                                           String data, Throwable e) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new IDPMgtBridgeServiceException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a IDPMgtBridgeServiceServerException from IdPConstants.ErrorMessages
     * object when no exception is thrown.
     *
     * @param error ConsentConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return IDPMgtBridgeServiceServerException.
     */
    public static IDPMgtBridgeServiceException handleServerException(IdPConstants.ErrorMessages error,
                                                                     String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }

        return new IDPMgtBridgeServiceException(message, error.getCode());
    }
}
