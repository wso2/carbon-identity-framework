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

package org.wso2.carbon.identity.api.resource.mgt;

import org.apache.commons.collections.CollectionUtils;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.api.resource.mgt.dao.AuthorizationDetailsTypeMgtDAO;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.AuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.dao.impl.CacheBackedAuthorizationDetailsTypeMgtDAOImpl;
import org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil;
import org.wso2.carbon.identity.api.resource.mgt.util.FilterQueriesUtil;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@link AuthorizationDetailsTypeManager} interface that provides
 * management functionalities for authorization detail types associated with APIs.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.api.resource.mgt.AuthorizationDetailsTypeManager",
                "service.scope=singleton"
        }
)
public class AuthorizationDetailsTypeManagerImpl implements AuthorizationDetailsTypeManager {

    private final AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO;

    public AuthorizationDetailsTypeManagerImpl() {

        this(new CacheBackedAuthorizationDetailsTypeMgtDAOImpl(new AuthorizationDetailsTypeMgtDAOImpl()));
    }

    public AuthorizationDetailsTypeManagerImpl(AuthorizationDetailsTypeMgtDAO authorizationDetailsTypeMgtDAO) {

        this.authorizationDetailsTypeMgtDAO = authorizationDetailsTypeMgtDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes, String tenantDomain)
            throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(authorizationDetailsTypes)) {
            return Collections.emptyList();
        }
        AuthorizationDetailsTypesUtil.assertRichAuthorizationRequestsEnabled();
        return this.authorizationDetailsTypeMgtDAO.addAuthorizationDetailsTypes(apiId, authorizationDetailsTypes,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId, String tenantDomain)
            throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return;
        }

        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypeByApiIdAndTypeId(apiId, typeId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return;
        }

        this.authorizationDetailsTypeMgtDAO.deleteAuthorizationDetailsTypesByApiId(apiId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(
            String apiId, String typeId, String tenantDomain) throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return null;
        }

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypeByApiIdAndTypeId(apiId, typeId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String filter, String tenantDomain)
            throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return Collections.emptyList();
        }

        return this.authorizationDetailsTypeMgtDAO.getAuthorizationDetailsTypes(FilterQueriesUtil.getExpressionNodes(
                filter, null, null), IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return Collections.emptyList();
        }

        return this.authorizationDetailsTypeMgtDAO
                .getAuthorizationDetailsTypesByApiId(apiId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorizationDetailsTypeExists(String filter, String tenantDomain) throws APIResourceMgtException {

        return CollectionUtils.isNotEmpty(this.getAuthorizationDetailsTypes(filter, tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorizationDetailsTypeExists(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return false;
        }

        return this.authorizationDetailsTypeMgtDAO
                .isAuthorizationDetailsTypeExists(apiId, type, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAuthorizationDetailsTypes(String apiId, List<String> removedAuthorizationDetailsTypes,
                                                List<AuthorizationDetailsType> addedAuthorizationDetailsTypes,
                                                String tenantDomain) throws APIResourceMgtException {

        if (CollectionUtils.isEmpty(removedAuthorizationDetailsTypes) &&
                CollectionUtils.isEmpty(addedAuthorizationDetailsTypes)) {
            return;
        }
        AuthorizationDetailsTypesUtil.assertRichAuthorizationRequestsEnabled();
        this.authorizationDetailsTypeMgtDAO.updateAuthorizationDetailsTypes(apiId, removedAuthorizationDetailsTypes,
                addedAuthorizationDetailsTypes, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAuthorizationDetailsType(String apiId, AuthorizationDetailsType authorizationDetailsType,
                                               String tenantDomain) throws APIResourceMgtException {

        if (authorizationDetailsType == null) {
            return;
        }
        AuthorizationDetailsTypesUtil.assertRichAuthorizationRequestsEnabled();
        this.authorizationDetailsTypeMgtDAO.updateAuthorizationDetailsTypes(apiId,
                Collections.singletonList(authorizationDetailsType), IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
