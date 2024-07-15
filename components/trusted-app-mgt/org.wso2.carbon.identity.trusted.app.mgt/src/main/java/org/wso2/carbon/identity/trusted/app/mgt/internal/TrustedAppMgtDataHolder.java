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

package org.wso2.carbon.identity.trusted.app.mgt.internal;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.trusted.app.mgt.services.TrustedAppMgtService;

/**
 * Data holder to manage trusted app data and services.
 */
public class TrustedAppMgtDataHolder {

    private HttpService httpService;
    private ApplicationManagementService applicationManagementService;
    private TrustedAppMgtService trustedAppMgtService;

    private static TrustedAppMgtDataHolder instance = new TrustedAppMgtDataHolder();

    private TrustedAppMgtDataHolder() {

    }

    public static TrustedAppMgtDataHolder getInstance() {

        return instance;
    }

    public HttpService getHttpService() {

        return httpService;
    }

    public void setHttpService(HttpService httpService) {

        this.httpService = httpService;
    }

    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    public TrustedAppMgtService getTrustedAppMgtService() {

        return trustedAppMgtService;
    }

    public void setTrustedAppMgtService(TrustedAppMgtService trustedAppMgtService) {

        this.trustedAppMgtService = trustedAppMgtService;
    }
}
