/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.api;


import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;

public class FrameworkRuntimeException extends IdentityRuntimeException {

    protected FrameworkRuntimeException(String errorDescription) {
        super(errorDescription);
    }

    protected FrameworkRuntimeException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }

    public static FrameworkRuntimeException error(String message) {
        return new FrameworkRuntimeException(message);
    }

    public static FrameworkRuntimeException error(String errorDescription, Throwable cause) {
        return new FrameworkRuntimeException(errorDescription, cause);
    }
}
