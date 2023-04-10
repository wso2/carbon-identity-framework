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

package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtService;
import org.wso2.carbon.identity.mgt.IdentityMgtServiceException;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;

/**
 * This service is to configure the Admin Advisory Management functionality.
 */
public class AdminAdvisoryManagementService {

    protected static final Log LOG = LogFactory.getLog(AdminAdvisoryManagementService.class);
    private final RegistryResourceMgtService resourceMgtService = IdentityMgtServiceComponent
            .getRegistryResourceMgtService();
    private static final String ADMIN_ADVISORY_BANNER_PATH = "identity/config/adminAdvisoryBanner";

    /**
     * This method is used to save the Admin advisory banner configurations which is specific to tenant.
     *
     * @param adminAdvisoryBanner - Admin advisory banner to be saved.
     * @throws IdentityMgtServiceException - Error while saving the admin advisory banner configuration.
     */
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBanner)
            throws IdentityMgtServiceException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Resource bannerResource = createAdminBannerRegistryResource(adminAdvisoryBanner);

        try {
            resourceMgtService.putIdentityResource(bannerResource, ADMIN_ADVISORY_BANNER_PATH, tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configuration saved successfully for tenant: " + tenantDomain);
            }
        } catch (Exception e) {
            String msg = "Error occurred while saving admin advisory banner configuration";
            throw new IdentityMgtServiceException(msg, e);
        }
    }

    /**
     * This method is used to load the tenant specific Admin advisory banner configurations.
     *
     * @return an AdminAdvisoryBannerDTO object.
     * @throws IdentityMgtServiceException - Error while loading the admin advisory banner configuration.
     */
    public AdminAdvisoryBannerDTO getAdminAdvisoryConfig() throws IdentityMgtServiceException {

        AdminAdvisoryBannerDTO adminAdvisoryBanner;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try {
            Resource registryResource = resourceMgtService
                    .getIdentityResource(ADMIN_ADVISORY_BANNER_PATH, tenantDomain);
            if (registryResource != null) {
                adminAdvisoryBanner = createAdminAdvisoryBannerDTO(registryResource);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Admin advisory banner configuration loaded successfully for tenant: " + tenantDomain);
                }
            } else {
                adminAdvisoryBanner = new AdminAdvisoryBannerDTO();
                adminAdvisoryBanner.setEnableBanner(IdentityMgtConstants.AdminAdvisory.ENABLE_BANNER_BY_DEFAULT);
                adminAdvisoryBanner.setBannerContent(IdentityMgtConstants.AdminAdvisory.DEFAULT_BANNER_CONTENT);
            }

        }  catch (Exception e) {
            String msg = "Error occurred while loading admin advisory banner configuration";
            throw new IdentityMgtServiceException(msg, e);
        }

        return adminAdvisoryBanner;
    }

    /**
     * This method is used to convert AdminAdvisoryBannerDTO to Resource object to be saved in registry.
     *
     * @return a Resource object.
     */
    private Resource createAdminBannerRegistryResource(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO) {

        // Set resource properties.
        Resource bannerResource = new ResourceImpl();
        bannerResource.setProperty(IdentityMgtConstants.AdminAdvisory.ENABLE_BANNER,
                String.valueOf(adminAdvisoryBannerDTO.getEnableBanner()));
        bannerResource.setProperty(IdentityMgtConstants.AdminAdvisory.BANNER_CONTENT,
                String.valueOf(adminAdvisoryBannerDTO.getBannerContent()));

        return bannerResource;
    }

    /**
     * This method is used to convert Resource object to AdminAdvisoryBannerDTO to be saved in registry.
     *
     * @return an AdminAdvisoryBannerDTO object.
     */
    private AdminAdvisoryBannerDTO createAdminAdvisoryBannerDTO(Resource bannerResource) {

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
        String enableBanner = bannerResource.getProperty(IdentityMgtConstants.AdminAdvisory.ENABLE_BANNER);
        String content = bannerResource.getProperty(IdentityMgtConstants.AdminAdvisory.BANNER_CONTENT);
        adminAdvisoryBannerDTO.setEnableBanner(Boolean.parseBoolean(enableBanner));
        adminAdvisoryBannerDTO.setBannerContent(content);

        return adminAdvisoryBannerDTO;
    }
}
