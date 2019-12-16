/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.store.configuration.utils;

/**
 * Exception class to capture client exception with error codes.
 */
public class IdentityUserStoreClientException extends IdentityUserStoreMgtException {

    private static final long serialVersionUID = 846424495576433422L;

    public IdentityUserStoreClientException(String message) {

        super(message);
    }

    public IdentityUserStoreClientException(String message, Throwable e) {

        super(message, e);
    }

    public IdentityUserStoreClientException(String errorCode, String message) {

        super(errorCode, message);
    }
}
