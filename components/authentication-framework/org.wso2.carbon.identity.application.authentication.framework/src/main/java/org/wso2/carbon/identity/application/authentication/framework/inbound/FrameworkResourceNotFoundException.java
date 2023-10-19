/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

/**
 * Framework resource not found exception.
 */
public class FrameworkResourceNotFoundException extends FrameworkException {

    public FrameworkResourceNotFoundException(String message) {
        super(message);
    }

    public FrameworkResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    public FrameworkResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameworkResourceNotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
