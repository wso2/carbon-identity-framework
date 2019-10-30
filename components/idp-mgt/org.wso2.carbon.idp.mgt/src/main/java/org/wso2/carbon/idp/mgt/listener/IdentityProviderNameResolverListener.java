/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.listener;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;

import java.util.Collection;

/**
 * Interceptor to invoke old Identity Provider related interceptors which requires IdP name to be passed.
 */
public class IdentityProviderNameResolverListener extends AbstractIdentityProviderMgtListener {

    private static CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());

    @Override
    public int getDefaultOrderId() {

        return 202;
    }

    public boolean doPreDeleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        // Get IDP by resourceId.
        IdentityProvider idp = dao.getIdPByResourceId(resourceId, IdentityTenantUtil.getTenantId
                (tenantDomain), tenantDomain);
        if (idp != null) {
            String idpName = idp.getIdentityProviderName();
            // Invoking the pre-delete listeners.
            Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
            for (IdentityProviderMgtListener listener : listeners) {
                if (listener.isEnable() && !listener.doPreDeleteIdP(idpName, tenantDomain)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean doPostDeleteIdPByResourceId(String resourceId, IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {

        if (identityProvider != null) {
            String idpName = identityProvider.getIdentityProviderName();

            // Invoking the post-delete listeners.
            Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
            for (IdentityProviderMgtListener listener : listeners) {
                if (listener.isEnable() && !listener.doPostDeleteIdP(idpName, tenantDomain)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean doPreUpdateIdPByResourceId(String resourceId, IdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        IdentityProvider idp = dao.getIdPByResourceId(resourceId, tenantId, tenantDomain);
        if (idp != null) {
            String oldIdPName = idp.getIdentityProviderName();

            // invoking the pre listeners
            Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
            for (IdentityProviderMgtListener listener : listeners) {
                if (listener.isEnable() && !listener.doPreUpdateIdP(oldIdPName, identityProvider, tenantDomain)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean doPostUpdateIdPByResourceId(String resourceId, IdentityProvider oldIdentityProvider,
                                               IdentityProvider newIdentityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        if (oldIdentityProvider != null) {
            String oldIdPName = oldIdentityProvider.getIdentityProviderName();

            // invoking the post listeners
            Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
            for (IdentityProviderMgtListener listener : listeners) {
                if (listener.isEnable() && !listener.doPostUpdateIdP(oldIdPName, newIdentityProvider, tenantDomain)) {
                    return false;
                }
            }
        }
        return true;
    }
}
