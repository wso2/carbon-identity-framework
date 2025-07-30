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

package org.wso2.carbon.identity.core.internal.context;

import org.apache.catalina.connector.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Organization;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.regex.Pattern;

/**
 * Utility class for managing organization operations for the IdentityContextCreatorValve.
 */
public class OrganizationResolver {

    private static final OrganizationResolver INSTANCE = new OrganizationResolver();
    private static final Log LOG = LogFactory.getLog(OrganizationResolver.class);
    private static final String TENANT_SEPARATOR = "/t/";
    private static final String ORG_SEPARATOR = "/o/";
    private static final Pattern PATTERN_ORG_QUALIFIED_ONLY =
            Pattern.compile("^/o/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}/?");
    private static final Pattern PATTERN_TENANT_AND_ORG_QUALIFIED =
            Pattern.compile("^/t/[^/]+/o/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}/?");

    private OrganizationResolver() {
        // Private constructor to prevent instantiation.
    }

    public static OrganizationResolver getInstance() {

        return INSTANCE;
    }

    /**
     * Initializes the organization information in the IdentityContext based on the request URI.
     *
     * @param request The HTTP request containing the URI.
     */
    public void resolveOrganizationInContext(Request request) {

        String requestURI = request.getRequestURI();
        if (StringUtils.isBlank(requestURI)) {
            return;
        }

        try {
            if (PATTERN_TENANT_AND_ORG_QUALIFIED.matcher(requestURI).find()) {
                // Handle the requests starts with /t/<tenant>/o/<org_id>/
                // Request URI has tenant domain -> Resolve Root organization info.
                String tenantDomainOfRootOrg = extractResourceFromURI(requestURI, TENANT_SEPARATOR);
                resolveRootOrganization(IdentityTenantUtil.getTenantId(tenantDomainOfRootOrg));

                // Request URI has an organization ID -> Resolve sub-organization info.
                String organizationId = extractResourceFromURI(requestURI, ORG_SEPARATOR);
                resolveOrganization(organizationId);
            } else if (PATTERN_ORG_QUALIFIED_ONLY.matcher(requestURI).find()) {
                // Handle the requests starts with /o/<org_id>/
                // Request URI has an organization ID -> Resolve both root and sub-organization info.
                resolveRootAndSubOrganization(requestURI);
            } else if (requestURI.contains(ORG_SEPARATOR)) {
                // Handle the requests starts with /o/ or /t/<tenant>/o/
                // /o/ -> Act as /t/carbon.super/o/
                // Resolve root organization with tenant information in the CarbonContext.
                // Resolving sub organization is handled in org.wso2.carbon.identity.authz.valve.AuthorizationValve.
                resolveRootOrganization(IdentityContext.getThreadLocalIdentityContext().getTenantId());
            } else {
                // Handle the requests starts with /t/<tenant>/ and the super tenant requests without /t/carbon.super/.
                // Resolve root organization with tenant information in the CarbonContext.
                resolveRootOrganization(IdentityContext.getThreadLocalIdentityContext().getTenantId());
                // Resolve root organization information to the Organization object in tenanted paths.
                resolveRootOrganizationToOrganization();
            }
        } catch (OrganizationManagementException | UserStoreException e) {
            LOG.error("Error while initializing organization information.", e);
        }
    }

    private void resolveOrganization(String organizationId) throws OrganizationManagementException {

        // When tenant domain is not provided, it will be resolved from the organization ID inside the
        // organization manager service.
        resolveOrganization(organizationId, null);
    }

    private void resolveOrganization(String organizationId, String associatedTenantDomain)
            throws OrganizationManagementException {

        MinimalOrganization minimalOrganization = getMinimalOrganization(organizationId, associatedTenantDomain);
        if (minimalOrganization == null) {
            LOG.debug("Unable to find an organization for the id: " + organizationId +
                    ". Cannot initialize organization.");
            return;
        }
        if (minimalOrganization.getDepth() <
                org.wso2.carbon.identity.organization.management.service.util.Utils.getSubOrgStartLevel()) {
            LOG.debug("Organization with id: " + organizationId + " is not a sub organization. " +
                    "Skipping initialization of organization.");
            return;
        }

        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(minimalOrganization.getId())
                .name(minimalOrganization.getName())
                .organizationHandle(minimalOrganization.getOrganizationHandle())
                .parentOrganizationId(minimalOrganization.getParentOrganizationId())
                .depth(minimalOrganization.getDepth())
                .build());
    }

    private void resolveRootOrganizationToOrganization() throws OrganizationManagementException {

        RootOrganization rootOrganization = IdentityContext.getThreadLocalIdentityContext().getRootOrganization();
        if (rootOrganization == null) {
            LOG.debug("Root organization is not set in the IdentityContext. Cannot initialize organization.");
            return;
        }

        String organizationId = rootOrganization.getOrganizationId();
        String associatedTenantDomain = rootOrganization.getAssociatedTenantDomain();
        MinimalOrganization minimalOrganization = getMinimalOrganization(organizationId, associatedTenantDomain);
        if (minimalOrganization == null) {
            LOG.debug("Unable to find an organization for the root organization id: " + organizationId +
                    ". Cannot initialize organization.");
            return;
        }

        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(minimalOrganization.getId())
                .name(minimalOrganization.getName())
                .organizationHandle(minimalOrganization.getOrganizationHandle())
                .parentOrganizationId(minimalOrganization.getParentOrganizationId())
                .depth(minimalOrganization.getDepth())
                .build());
    }

    private void resolveRootAndSubOrganization(String requestURI) throws OrganizationManagementException {

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

        resolveOrganization(organizationId);
    }

    private void resolveRootOrganization(int tenantId) throws UserStoreException {

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

    private MinimalOrganization getMinimalOrganization(String organizationId, String associatedTenantDomain)
            throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .getMinimalOrganization(organizationId, associatedTenantDomain);
    }

    private String getRootOrganizationId(String organizationId) throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .getPrimaryOrganizationId(organizationId);
    }

    private String getOrganizationTenantDomain(String organizationId) throws OrganizationManagementException {

        return IdentityCoreServiceDataHolder.getInstance().getOrganizationManager()
                .resolveTenantDomain(organizationId);
    }

    private String extractResourceFromURI(String requestURI, String resourceIdentifier) {

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
