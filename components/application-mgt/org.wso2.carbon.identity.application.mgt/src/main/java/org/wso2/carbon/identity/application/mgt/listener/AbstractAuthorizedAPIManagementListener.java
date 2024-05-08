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

package org.wso2.carbon.identity.application.mgt.listener;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;

import java.util.List;

/**
 * Authorized API management listener.
 */
public abstract class AbstractAuthorizedAPIManagementListener implements AuthorizedAPIManagementListener {

    @Override
    public int getExecutionOrderId() {
        return 0;
    }

    @Override
    public int getDefaultOrderId() {
        return 0;
    }

    @Override
    public boolean isEnable() {
        return false;
    }

    @Override
    public void preAddAuthorizedAPI(String appId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void postAddAuthorizedAPI(String appId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void preDeleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void postDeleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void preGetAuthorizedAPIs(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void postGetAuthorizedAPIs(List<AuthorizedAPI> authorizedAPIList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void prePatchAuthorizedAPI(String appId, String apiId, List<String> addedScopes, List<String> removedScopes,
                                      String tenantDomain) throws IdentityApplicationManagementException {

    }

    @Override
    public void postPatchAuthorizedAPI(String appId, String apiId, List<String> addedScopes, List<String> removedScopes,
                                       String tenantDomain) throws IdentityApplicationManagementException {

    }

    @Override
    public void preGetAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void postGetAuthorizedScopes(List<AuthorizedScopes> authorizedScopesList, String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public void preGetAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

    }

    @Override
    public AuthorizedAPI postGetAuthorizedAPI(AuthorizedAPI authorizedAPI, String appId, String apiId,
                                              String tenantDomain) throws IdentityApplicationManagementException {
        return null;
    }
}
