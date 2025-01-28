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

package org.wso2.carbon.identity.remote.log.publish.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.remote.log.publish.constants.RemoteLogPublishConstants;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishClientException;
import org.wso2.carbon.identity.remote.log.publish.exception.RemoteLogPublishServerException;

/**
 * Utility class for Action Management.
 */
public class RemoteLogPublishUtil {

    /**
     * Handle Action Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return RemoteLogPublishClientException.
     */
    public static RemoteLogPublishClientException handleClientException(
            RemoteLogPublishConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new RemoteLogPublishClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle Action Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return RemoteLogPublishServerException.
     */
    public static RemoteLogPublishServerException handleServerException(
            RemoteLogPublishConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new RemoteLogPublishServerException(error.getMessage(), description, error.getCode(), e);
    }
}
