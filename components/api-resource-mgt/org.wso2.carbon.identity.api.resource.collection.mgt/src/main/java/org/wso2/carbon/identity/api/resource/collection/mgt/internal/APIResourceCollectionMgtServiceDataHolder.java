/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.collection.mgt.internal;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;

/**
 * API Resource Collection Management Service Data Holder.
 */
public class APIResourceCollectionMgtServiceDataHolder {

    private APIResourceManager apiResourceManagementService = null;
    private static final APIResourceCollectionMgtServiceDataHolder INSTANCE =
            new APIResourceCollectionMgtServiceDataHolder();

    public static APIResourceCollectionMgtServiceDataHolder getInstance() {

        return INSTANCE;
    }

    public APIResourceManager getAPIResourceManagementService() {

        return apiResourceManagementService;
    }

    public void setAPIResourceManagementService(APIResourceManager apiResourceManagementService) {

        this.apiResourceManagementService = apiResourceManagementService;
    }
}
