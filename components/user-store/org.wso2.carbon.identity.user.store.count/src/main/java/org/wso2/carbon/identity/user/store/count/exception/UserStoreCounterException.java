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
package org.wso2.carbon.identity.user.store.count.exception;

public class UserStoreCounterException extends Exception {

    private static final long serialVersionUID = 8260611169153946414L;
    private String message;

    public UserStoreCounterException() {
        super();
    }

    public UserStoreCounterException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public UserStoreCounterException(String message, boolean convertMessage) {
        super(message);
        this.message = message;
    }


    public UserStoreCounterException(String message) {
        super(message);
        this.message = message;
    }

    public UserStoreCounterException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return message;
    }

}
