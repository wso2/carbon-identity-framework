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

package org.wso2.carbon.identity.application.common.model;

/**
 * The response class for the import service provider method. It has the response code as "201" if the application
 * imported successfully and "400" if there are any errors in the importing process. If created it has the
 * application name else the list of errors.
 */
public class ImportResponse {

    public static final int CREATED = 201;
    public static final int FAILED = 400;

    private int responseCode;

    private String applicationName;

    private String[] errors;

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    public String[] getErrors() {

        return errors;
    }

    public void setErrors(String[] errors) {

        this.errors = errors;
    }

    public int getResponseCode() {

        return responseCode;
    }

    public void setResponseCode(int responseCode) {

        this.responseCode = responseCode;
    }
}
