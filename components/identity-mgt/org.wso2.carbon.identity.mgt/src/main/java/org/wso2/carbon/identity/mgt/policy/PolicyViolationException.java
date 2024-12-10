/*
 * Copyright (c) 2014-2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.policy;

import org.wso2.carbon.identity.base.IdentityException;

public class PolicyViolationException extends IdentityException {

    private static final long serialVersionUID = 7267202484738844205L;

    /**
     * Constructs a PolicyViolationException with the specified error message.
     *
     * @param message the detail message to describe the violation.
     */
    public PolicyViolationException(String message) {
    
        super(message);
    }
    
    /**
     * Constructs a PolicyViolationException with the specified error code and message.
     *
     * @param errorCode the specific error code for this violation.
     * @param message   the detail message to describe the violation.
     */
    public PolicyViolationException(String errorCode, String message) {
    
        super(errorCode, message);
    }
    
    /**
     * Constructs a PolicyViolationException with the specified message and cause.
     *
     * @param message the detail message to describe the violation.
     * @param e       the cause of this exception.
     */
    public PolicyViolationException(String message, Throwable e) {
    
        super(message, e);
    }
}
