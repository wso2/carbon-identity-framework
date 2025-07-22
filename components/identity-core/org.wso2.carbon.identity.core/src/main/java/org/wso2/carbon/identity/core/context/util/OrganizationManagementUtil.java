/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.context.util;

import org.apache.catalina.connector.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Organization;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for managing organization operations for the IdentityContextCreatorValve.
 */
public class OrganizationManagementUtil {

    private static final Log LOG = LogFactory.getLog(OrganizationManagementUtil.class);
    private static final String TENANT_SEPARATOR = "/t/";
    private static final String ORG_SEPARATOR = "/o/";
    private static final Pattern PATTERN_ORG_CONTEXT = Pattern.compile("^/o/[a-f0-9\\-]+?");
    private static final Pattern PATTERN_ORG_CONTEXT_IN_TENANT_PERSPECTIVE =
            Pattern.compile("^/t/[^/]+/o/[a-f0-9\\-]+?");

    private OrganizationManagementUtil() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Initializes the organization information in the IdentityContext based on the request URI.
     *
     * @param request The HTTP request containing the URI.
     */
    public static void initOrganizationInfo(Request request) {

        String requestURI = request.getRequestURI();
        if (StringUtils.isBlank(requestURI)) {
            return;
        }

        try {
            if (PATTERN_ORG_CONTEXT_IN_TENANT_PERSPECTIVE.matcher(requestURI).find()) {
                // Handle the requests starts with /t/<tenant>/o/<org_id>/
                // Request URI has tenant domain -> Resolve Root organization info.
                String tenantDomainOfRootOrg = extractResourceFromURI(requestURI, TENANT_SEPARATOR);
                initRootOrganization(IdentityTenantUtil.getTenantId(tenantDomainOfRootOrg));

                // Request URI has an organization ID -> Resolve sub-organization info.
                String organizationId = extractResourceFromURI(requestURI, ORG_SEPARATOR);
                initOrganization(organizationId);
            } else if (PATTERN_ORG_CONTEXT.matcher(requestURI).find()) {
                // Handle the requests starts with /o/<org_id>/
                // Request URI has an organization ID -> Resolve both root and sub-organization info.
                initRootAndSubOrganization(requestURI);
            } else {
                // Handle the requests starts with /t/<tenant>/, /o/ or /t/<tenant>/o/
                // Resolve root organization with tenant information in the CarbonContext.
                // Resolving sub organization is handled in org.wso2.carbon.identity.authz.valve.AuthorizationValve.
                initRootOrganization(IdentityContext.getThreadLocalIdentityContext().getTenantId());
            }
        } catch (OrganizationManagementException | UserStoreException e) {
            LOG.error("Error while initializing organization information.", e);
        }
    }

    private static void initOrganization(String organizationId) throws OrganizationManagementException {

        int organizationDepth = getOrganizationDepthInHierarchy(organizationId);
        if (organizationDepth <
                org.wso2.carbon.identity.organization.management.service.util.Utils.getSubOrgStartLevel()) {
            LOG.debug("Organization with id: " + organizationId + " is not a sub organization. " +
                    "Skipping initialization of organization.");
            return;
        }

        BasicOrganization basicOrganizationInfo = getBasicOrganization(organizationId);
        if (basicOrganizationInfo == null) {
            LOG.debug("Unable to find an organization for the id: " + organizationId +
                    ". Cannot initialize organization.");
            return;
        }
        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(basicOrganizationInfo.getId())
                .name(basicOrganizationInfo.getName())
                .organizationHandle(basicOrganizationInfo.getOrganizationHandle())
                .depth(organizationDepth)
                .build());
    }

    private static void initRootAndSubOrganization(String requestURI) throws OrganizationManagementException {

        String organizationId = extractResourceFromURI(requestURI, ORG_SEPARATOR);
        String organizationIdOfRootOrg = getRootOrganizationId(organizationId);

        if (OrganizationManagementConstants.SUPER_ORG_ID.equals(organizationIdOfRootOrg)) {
            // If the root organization is the super organization, we can set it directly.
            IdentityContext.getThreadLocalIdentityContext().setRootOrganization(new RootOrganization.Builder()
                    .associatedTenantId(MultitenantConstants.SUPER_TENANT_ID)
                    .associatedTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                    .organizationId(OrganizationManagementConstants.SUPER_ORG_ID)
                    .build());
        } else {
            String tenantDomainOfRootOrg = getOrganizationTenantDomain(organizationIdOfRootOrg);
            IdentityContext.getThreadLocalIdentityContext().setRootOrganization(new RootOrganization.Builder()
                    .associatedTenantId(IdentityTenantUtil.getTenantId(tenantDomainOfRootOrg))
                    .associatedTenantDomain(tenantDomainOfRootOrg)
                    .organizationId(organizationIdOfRootOrg)
                    .build());
        }

        initOrganization(organizationId);
    }

    private static void initRootOrganization(int tenantId) throws UserStoreException {

        if (MultitenantConstants.SUPER_TENANT_ID == tenantId) {
            IdentityContext.getThreadLocalIdentityContext().setRootOrganization(new RootOrganization.Builder()
                    .associatedTenantId(MultitenantConstants.SUPER_TENANT_ID)
                    .associatedTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                    .organizationId(OrganizationManagementConstants.SUPER_ORG_ID)
                    .build());
            return;
        }

        Tenant tenant = IdentityCoreServiceDataHolder.getInstance().getRealmService().getTenantManager()
                .getTenant(tenantId);
        if (tenant == null) {
            LOG.debug("Tenant with id: " + tenantId + " does not exist. Cannot initialize root organization.");
            return;
        }

        IdentityContext.getThreadLocalIdentityContext().setRootOrganization(new RootOrganization.Builder()
                .associatedTenantId(tenantId)
                .associatedTenantDomain(IdentityContext.getThreadLocalIdentityContext().getTenantDomain())
                .organizationId(tenant.getAssociatedOrganizationUUID())
                .build());
    }

    private static int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .getOrganizationDepthInHierarchy(organizationId);
    }

    private static BasicOrganization getBasicOrganization(String organizationId) throws OrganizationManagementException {

        Map<String, BasicOrganization> orgsMap = IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .getBasicOrganizationDetailsByOrgIDs(Collections.singletonList(organizationId));
        return orgsMap.get(organizationId);
    }

    private static String getRootOrganizationId(String organizationId) throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .getPrimaryOrganizationId(organizationId);
    }

    private static String getOrganizationTenantDomain(String organizationId) throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .resolveTenantDomain(organizationId);
    }

    private static String extractResourceFromURI(String requestURI, String resourceIdentifier) {

        int startIndex = requestURI.indexOf(resourceIdentifier) + resourceIdentifier.length();
        if (startIndex < resourceIdentifier.length() || startIndex >= requestURI.length()) {
            return null;
        }

        int endIndex = requestURI.indexOf("/", startIndex);
        if (endIndex == -1) {
            return requestURI.substring(startIndex);
        }

        return requestURI.substring(startIndex, endIndex);
    }
}
