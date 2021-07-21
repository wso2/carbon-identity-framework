/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;

/**
 * Session Serializer Provider.
 */
public class SessionSerializerProvider {

    private static final Log log = LogFactory.getLog(SessionSerializerProvider.class);

    /**
     * Get Session Serializer.
     *
     * @param name Session Serializer name.
     * @return Session Serializer
     */
    public static SessionSerializer getSessionSerializer(String name) throws IdentityApplicationManagementException {

        if (name == null) {
            String errMsg = "SessionSerializer name cannot be null";
            log.error(errMsg);
            throw new IdentityApplicationManagementException(errMsg);
        }

        SessionSerializer serializer = null;

        for (SessionSerializer sessionSerializer : FrameworkServiceDataHolder.getInstance().getSessionSerializers()) {

            if (sessionSerializer.getName().equals(name)) {
                serializer = sessionSerializer;
            }
        }

        return serializer;
    }
}
