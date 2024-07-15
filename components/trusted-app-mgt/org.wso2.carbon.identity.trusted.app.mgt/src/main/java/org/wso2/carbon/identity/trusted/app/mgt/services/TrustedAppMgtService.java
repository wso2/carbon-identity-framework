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

package org.wso2.carbon.identity.trusted.app.mgt.services;

import org.wso2.carbon.identity.trusted.app.mgt.exceptions.TrustedAppMgtException;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedAndroidApp;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedIosApp;

import java.util.List;

/**
 * This interface defines methods for managing trusted applications, which involves retrieving trusted app
 * details based on the platform.
 */
public interface TrustedAppMgtService {

    /**
     * Retrieves a list of trusted Android applications of all tenants.
     *
     * @return The list of trusted Android applications.
     */
    List<TrustedAndroidApp> getTrustedAndroidApps() throws TrustedAppMgtException;

    /**
     * Retrieves a list of trusted iOS applications of all tenants.
     *
     * @return The list of trusted iOS applications.
     */
    List<TrustedIosApp> getTrustedIosApps() throws TrustedAppMgtException;

}

