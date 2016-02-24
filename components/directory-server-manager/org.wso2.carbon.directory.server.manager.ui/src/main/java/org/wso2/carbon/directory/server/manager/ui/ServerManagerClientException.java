/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.directory.server.manager.ui;

/**
 * An exception class to wrap client side errors in directory server component.
 * Also this maintains error codes.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ServerManagerClientException extends Exception {

    public static final String LIST_SERVICE_PRINCIPLES_ERROR = "service.principle.list.error";
    public static final String ADD_SERVICE_PRINCIPLES_ERROR = "service.principle.add.error";
    public static final String REMOVE_SERVICE_PRINCIPLES_ERROR = "service.principle.remove.error";
    public static final String CHANGE_SERVICE_PRINCIPLES_ERROR = "service.principle.change.error";
    public static final String INIT_SERVICE_PRINCIPLE_ERROR = "service.principle.init.error";
    public static final String SERVICE_PRINCIPLE_ALREADY_EXISTS = "service.principle.exists";
    public static final String PASSWORD_FORMAT_RETRIEVING_ERROR = "password.format.get.error";
    public static final String NAME_FORMAT_RETRIEVING_ERROR = "name.format.get.error";

    public ServerManagerClientException() {
        super();
    }

    public ServerManagerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerManagerClientException(String message) {
        super(message);
    }

    public ServerManagerClientException(Throwable cause) {
        super(cause);
    }
}
