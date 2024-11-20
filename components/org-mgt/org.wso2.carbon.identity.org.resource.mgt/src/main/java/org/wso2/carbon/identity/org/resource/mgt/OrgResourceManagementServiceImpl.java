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

package org.wso2.carbon.identity.org.resource.mgt;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementException;
import org.wso2.carbon.identity.org.resource.mgt.exceptions.OrgResourceManagementServerException;
import org.wso2.carbon.identity.org.resource.mgt.internal.OrgResourceManagementServiceDataHolder;
import org.wso2.carbon.identity.org.resource.mgt.strategy.AggregationStrategy;
import org.wso2.carbon.identity.org.resource.mgt.util.OrgResourceManagementUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of the OrgResourceManagementService.
 */
public class OrgResourceManagementServiceImpl implements OrgResourceManagementService {

    @Override
    public <T> T getResourcesFromOrgHierarchy(String organizationId, Function<String, Optional<T>> resourceRetriever,
                                              AggregationStrategy<T> aggregationStrategy)
            throws OrgResourceManagementException {

        try {
            OrganizationManager organizationManager = OrgResourceManagementUtil.getOrganizationManager();
            List<String> organizationIds = organizationManager.getAncestorOrganizationIds(organizationId);

            if (CollectionUtils.isEmpty(organizationIds) || organizationIds.isEmpty()) {
                return null;
            }

            return aggregationStrategy.aggregate(organizationIds, resourceRetriever);
        } catch (OrganizationManagementException e) {
            String errorMsg = String.format(
                    "Unexpected server error occurred while resolving resource for organization with id: %s",
                    organizationId);
            throw new OrgResourceManagementServerException(errorMsg, e);
        }
    }

    @Override
    public <T> T getResourcesFromOrgHierarchy(String organizationId, String applicationId,
                                              BiFunction<String, String, Optional<T>> resourceRetriever,
                                              AggregationStrategy<T> aggregationStrategy)
            throws OrgResourceManagementException {

        try {
            OrganizationManager organizationManager = OrgResourceManagementUtil.getOrganizationManager();
            List<String> organizationIds = organizationManager.getAncestorOrganizationIds(organizationId);

            if (CollectionUtils.isEmpty(organizationIds) || organizationIds.isEmpty()) {
                return null;
            }

            ApplicationManagementService applicationManagementService = getApplicationManagementService();
            Map<String, String> ancestorAppIds = Collections.emptyMap();
            if (applicationId != null) {
                ancestorAppIds = applicationManagementService.getAncestorAppIds(applicationId, organizationId);
            }

            return aggregationStrategy.aggregate(organizationIds, ancestorAppIds, resourceRetriever);
        } catch (OrganizationManagementException | IdentityApplicationManagementException e) {
            String errorMsg = String.format(
                    "Unexpected server error occurred while resolving resource for organization with id: %s and " +
                            "application id: %s.", organizationId, applicationId);
            throw new OrgResourceManagementServerException(errorMsg, e);
        }
    }

    private ApplicationManagementService getApplicationManagementService() {

        return OrgResourceManagementServiceDataHolder.getInstance().getApplicationManagementService();
    }
}
