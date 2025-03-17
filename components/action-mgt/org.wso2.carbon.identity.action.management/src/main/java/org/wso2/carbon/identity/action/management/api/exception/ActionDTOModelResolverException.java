/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.action.management.api.exception;

/**
 * Exception class for ActionDTO Model Resolver.
 * This exception is thrown when there is an issue in performing action type specific operations.
 */
public class ActionDTOModelResolverException extends Exception {

    private String description;

    public ActionDTOModelResolverException(String message, String description) {

        super(message);
        this.description = description;
    }

    public ActionDTOModelResolverException(String message, Throwable cause) {

        super(message, cause);
    }

    public ActionDTOModelResolverException(String message, String description, Throwable cause) {

        super(message, cause);
        this.description = description;
    }

    public String getDescription() {

        return this.description;
    }
}
