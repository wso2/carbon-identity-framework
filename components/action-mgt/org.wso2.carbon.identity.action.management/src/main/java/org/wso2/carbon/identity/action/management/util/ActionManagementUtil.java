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

package org.wso2.carbon.identity.action.management.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtRuntimeException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;

/**
 * Utility class for Action Management.
 */
public class ActionManagementUtil {

    /**
     * Handle Action Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return ActionMgtClientException.
     */
    public static ActionMgtClientException handleClientException(
            ActionMgtConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ActionMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle Action Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return ActionMgtServerException.
     */
    public static ActionMgtServerException handleServerException(
            ActionMgtConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new ActionMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Handle Action Management runtime exceptions.
     *
     * @param e Throwable.
     * @return ActionMgtRuntimeException.
     */
    public static ActionMgtRuntimeException handleRuntimeException(String errorMessage, Throwable e) {

        return new ActionMgtRuntimeException(errorMessage, e);
    }
}
