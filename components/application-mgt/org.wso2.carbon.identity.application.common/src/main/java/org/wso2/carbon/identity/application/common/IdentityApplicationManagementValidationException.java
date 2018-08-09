/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.common;

/**
 * Exception class handling exceptions during application validation. Validation errors have to added to the
 * validationMsg list.
 */
public class IdentityApplicationManagementValidationException extends IdentityApplicationManagementException {

    private static final long serialVersionUID = 546145354402013968L;

    private String[] validationMsg;

    public IdentityApplicationManagementValidationException(String[] validationMsg) {

        super("Validation Error");
        this.validationMsg = validationMsg;
    }

    public String[] getValidationMsg() {

        return validationMsg;
    }
}
