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

package org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt;

import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;

/**
 * This class is used to define the server side errors which needs to be handled.
 */
public class SessionManagementServerException extends SessionManagementException {

    public SessionManagementServerException(SessionMgtConstants.ErrorMessages error, String description,
                                            Throwable cause) {

        super(error, description, cause);
    }

    public SessionManagementServerException(String message, String errorCode, String description, Throwable cause) {

        super(message, errorCode, description, cause);
    }
}
