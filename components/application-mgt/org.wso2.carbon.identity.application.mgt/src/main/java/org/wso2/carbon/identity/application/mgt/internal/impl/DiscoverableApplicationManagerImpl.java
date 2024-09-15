/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt.internal.impl;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.DiscoverableApplicationManager;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.util.List;

/**
 * Implementation of the {@link DiscoverableApplicationManager}.
 */
public class DiscoverableApplicationManagerImpl implements DiscoverableApplicationManager {

    @Override
    public List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String
            filter, String sortOrder, String sortBy, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            if (ApplicationMgtUtil.isSubOrg(tenantDomain)) {
                String primaryOrgID = ApplicationMgtUtil.getParentOrgId(tenantDomain);
                return appDAO.getDiscoverableAppsInfoFromRootAndSubOrg(limit, offset, filter, sortOrder, sortBy,
                        tenantDomain, primaryOrgID);
            } else {
                return appDAO.getDiscoverableApplicationBasicInfo(limit, offset, filter, sortOrder, sortBy,
                        tenantDomain);
            }
        } catch (OrganizationManagementException e) {
            throw new IdentityApplicationManagementException(e.getErrorMessage(), e);
        }
    }

    @Override
    public ApplicationBasicInfo getDiscoverableApplicationBasicInfoByResourceId(String resourceId, String
            tenantDomain) throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getDiscoverableApplicationBasicInfoByResourceId(resourceId, tenantDomain);
    }

    @Override
    public boolean isApplicationDiscoverable(String resourceId, String tenantDomain) throws
            IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.isApplicationDiscoverable(resourceId, tenantDomain);
    }

    @Override
    public int getCountOfDiscoverableApplications(String filter, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            if (ApplicationMgtUtil.isSubOrg(tenantDomain)) {
                String primaryOrgId = ApplicationMgtUtil.getParentOrgId(tenantDomain);
                return appDAO.getCountOfDiscoverableAppsFromRootAndSubOrg(filter, tenantDomain, primaryOrgId);
            } else {
                return appDAO.getCountOfDiscoverableApplications(filter, tenantDomain);
            }
        } catch (OrganizationManagementException e) {
            throw new IdentityApplicationManagementException(e.getErrorMessage(), e);
        }
    }
}
