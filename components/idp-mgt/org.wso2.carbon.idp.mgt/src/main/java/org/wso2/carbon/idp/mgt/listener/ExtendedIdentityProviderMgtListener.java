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

import org.wso2.carbon.identity.application.common.model.ExtendedIdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;

import java.util.Collection;

/**
 * Interceptor to invoke old Identity Provider related interceptors which requires IdP name to be passed.
 */
public class ExtendedIdentityProviderMgtListener extends AbstractIdentityProviderMgtListener {

    private static CacheBackedIdPMgtDAO dao = new CacheBackedIdPMgtDAO(new IdPManagementDAO());

    @Override
    public int getDefaultOrderId() {

        return 202;
    }

    public boolean doPreDeleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        // get IDP by resourceId
        ExtendedIdentityProvider idp = dao.getIdPByResourceId(null, resourceId, IdentityTenantUtil.getTenantId
                (tenantDomain), tenantDomain);
        String idpName = idp.getIdentityProviderName();
        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteIdP(idpName, tenantDomain)) {
                return false;
            }
        }
        return true;
    }

    public boolean doPostDeleteIdPByResourceId(String resourceId, String tenantDomain) throws
            IdentityProviderManagementException {

        // get IDP by resourceId
        ExtendedIdentityProvider idp = dao.getIdPByResourceId(null, resourceId, IdentityTenantUtil.getTenantId
                (tenantDomain), tenantDomain);
        String idpName = idp.getIdentityProviderName();

        // invoking the post listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteIdP(idpName, tenantDomain)) {
                return false;
            }
        }
        return true;
    }

    public boolean doPreUpdateIdPByResourceId(String resourceId, ExtendedIdentityProvider identityProvider, String
            tenantDomain) throws IdentityProviderManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        ExtendedIdentityProvider idp = dao.getIdPByResourceId(null, resourceId, tenantId, tenantDomain);
        String oldIdPName = idp.getIdentityProviderName();

        // invoking the pre listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateIdP(oldIdPName, identityProvider, tenantDomain)) {
                return false;
            }
        }
        return true;
    }

    public boolean doPostUpdateIdPByResourceId(String resourceId, ExtendedIdentityProvider oldIdentityProvider,
                                   ExtendedIdentityProvider newIdentityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        String oldIdPName = oldIdentityProvider.getIdentityProviderName();

        // invoking the post listeners
        Collection<IdentityProviderMgtListener> listeners = IdPManagementServiceComponent.getIdpMgtListeners();
        for (IdentityProviderMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateIdP(oldIdPName, newIdentityProvider, tenantDomain)) {
                return false;
            }
        }
        return true;
    }
}
