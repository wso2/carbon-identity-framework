/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.persistence;

import org.wso2.carbon.identity.base.IdentityRuntimeException;

/**
 * Custom exception used to handle DB connection unavailability issues
 */
public class DBConnectionException extends IdentityRuntimeException {

    public DBConnectionException(String message) {

        super(message);
    }

    public DBConnectionException(String errorCode, Throwable cause) {

        super(errorCode, cause);
    }

    public static DBConnectionException error(String message) {

        return new DBConnectionException(message);
    }

    public static DBConnectionException error(String errorDescription, Throwable cause) {

        return new DBConnectionException(errorDescription, cause);
    }
}

