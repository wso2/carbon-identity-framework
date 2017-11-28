/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.core.migrate;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * This exception will be thrown in case of an error in migration client.
 */
public class MigrationClientException extends IdentityException {
    public MigrationClientException(String message) {
        super(message);
    }

    public MigrationClientException(String errorCode, String message) {
        super(errorCode, message);
    }

    public MigrationClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationClientException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
