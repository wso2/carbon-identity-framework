/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.dao;

import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;

import java.util.Map;

/**
 * Perform operations for {@link UserSession}.
 */
public interface UserSessionDAO {

    UserSession getSession(String sessionId) throws SessionManagementServerException;

    /**
     * Get federated authentication session mapping info during the federated idp init logout
     *
     * @param fedIdpSessionId
     * @return A map containing federated authentication session details
     * @throws SessionManagementServerException
     */
    Map<String, String> getSessionDetails(String fedIdpSessionId) throws SessionManagementServerException;
}
