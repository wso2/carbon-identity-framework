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

package org.wso2.carbon.identity.system.config.mgt.advisory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.advisory.mgt.dao.AdminAdvisoryBannerDAO;
import org.wso2.carbon.admin.advisory.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.system.config.mgt.cache.AdminAdvisoryBannerCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to manage storage of the Admin Advisory Banner configurations in the configuration-mgt store.
 */
public class DBBasedAdminBannerDAO implements AdminAdvisoryBannerDAO {

    private static final Log LOG = LogFactory.getLog(DBBasedAdminBannerDAO.class);
    AdminAdvisoryBannerCache advisoryBannerCache = AdminAdvisoryBannerCache.getInstance();

    public static final String ADVISORY_BANNER_RESOURCE_TYPE = "ADMIN_ADVISORY_BANNER";
    public static final String ADVISORY_BANNER_RESOURCE_NAME = "ADMIN_ADVISORY_BANNER_RESOURCE";
    public static final String ENABLE_BANNER = "enableBanner";
    public static final String BANNER_CONTENT = "bannerContent";
    public static final String RESOURCE_NOT_EXISTS_ERROR_CODE = "CONFIGM_00017";
    public static final String CACHE_KEY = "DBBasedAdminBannerCacheKey";

    private ConfigurationManager configurationManager;

    public DBBasedAdminBannerDAO(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    @Override
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO, String tenantDomain)
            throws AdminAdvisoryMgtException {

        Resource resource = buildResourceFromAdvisoryBannerDTO(adminAdvisoryBannerDTO);
        try {
            if (isAdminAdvisoryResourceExists(ADVISORY_BANNER_RESOURCE_TYPE, ADVISORY_BANNER_RESOURCE_NAME)) {
                this.configurationManager.replaceResource(ADVISORY_BANNER_RESOURCE_TYPE, resource);
            } else {
                this.configurationManager.addResource(ADVISORY_BANNER_RESOURCE_TYPE, resource);
            }

            Pair<Boolean, String> valueToCache =
                    Pair.of(adminAdvisoryBannerDTO.getEnableBanner(), adminAdvisoryBannerDTO.getBannerContent());
            advisoryBannerCache.addToCache(CACHE_KEY, valueToCache, tenantDomain);
        } catch (ConfigurationManagementException e) {
            throw new AdminAdvisoryMgtException("Error occurred while saving advisory banner configuration.", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configuration saved successfully in configuration-store for tenant: "
                    + tenantDomain);
        }
    }

    @Override
    public Optional<AdminAdvisoryBannerDTO> loadAdminAdvisoryConfig(String tenantDomain)
            throws AdminAdvisoryMgtException {

        Pair<Boolean, String> valueFromCache =  advisoryBannerCache.getValueFromCache(CACHE_KEY, tenantDomain);
        if (valueFromCache != null) {
            AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
            adminAdvisoryBannerDTO.setEnableBanner(valueFromCache.getKey());
            adminAdvisoryBannerDTO.setBannerContent(valueFromCache.getValue());
            return Optional.of(adminAdvisoryBannerDTO);
        }

        try {
            Resource resource = this.configurationManager.getResource(ADVISORY_BANNER_RESOURCE_TYPE,
                    ADVISORY_BANNER_RESOURCE_NAME);
            if (resource == null) {
                return Optional.empty();
            }

            List<ResourceFile> resourceFiles = this.configurationManager.getFiles(ADVISORY_BANNER_RESOURCE_TYPE,
                    ADVISORY_BANNER_RESOURCE_NAME);

            if (resourceFiles.isEmpty() || StringUtils.isBlank(resourceFiles.get(0).getId())) {
                return Optional.empty();
            }
            ResourceFile resourceFile = resourceFiles.get(0);
            if (resourceFile == null || StringUtils.isBlank(resourceFile.getId())) {
                return Optional.empty();
            }

            InputStream inputStream = this.configurationManager.getFileById
                    (ADVISORY_BANNER_RESOURCE_TYPE, ADVISORY_BANNER_RESOURCE_NAME, resourceFile.getId());
            AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = buildAdvisoryBannerDTOFromResource(resource, inputStream);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configuration loaded successfully from configuration-store for" +
                        " tenant: " + tenantDomain);
            }

            Pair<Boolean, String> valueToCache =
                    Pair.of(adminAdvisoryBannerDTO.getEnableBanner(), adminAdvisoryBannerDTO.getBannerContent());
            advisoryBannerCache.addToCache(CACHE_KEY, valueToCache, tenantDomain);

            return Optional.of(adminAdvisoryBannerDTO);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Can not find a admin advisory banner configurations for tenant: " + tenantDomain, e);
                }
                return Optional.empty();
            }
            throw new AdminAdvisoryMgtException("Error occurred while loading advisory banner configuration.", e);
        }
    }

    /**
     * Check whether the admin advisory resource exists for the current tenant.
     *
     * @param resourceType Admin Advisory Banner resource type.
     * @param resourceName Admin Advisory Banner resource name.
     * @return Return true if the resource already exists. If not return false.
     */
    private boolean isAdminAdvisoryResourceExists(String resourceType, String resourceName)
            throws AdminAdvisoryMgtException {

        Resource resource;
        try {
            resource = this.configurationManager.getResource(resourceType, resourceName);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                return false;
            }
            throw new AdminAdvisoryMgtException("Error occurred while checking the existence of the admin advisory " +
                    "resource.", e);
        }
        return resource != null;
    }

    private AdminAdvisoryBannerDTO buildAdvisoryBannerDTOFromResource(Resource resource, InputStream inputStream)
            throws AdminAdvisoryMgtException {

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
        adminAdvisoryBannerDTO.setEnableBanner(Boolean.parseBoolean(resource.getAttributes().get(0).getValue()));
        String bannerContent;

        try {
            bannerContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AdminAdvisoryMgtException("Error occurred while converting banner content to string.", e);
        }
        adminAdvisoryBannerDTO.setBannerContent(bannerContent);
        return adminAdvisoryBannerDTO;
    }

    private Resource buildResourceFromAdvisoryBannerDTO(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO)
            throws AdminAdvisoryMgtException {

        Resource resource = new Resource(ADVISORY_BANNER_RESOURCE_NAME, ADVISORY_BANNER_RESOURCE_TYPE);
        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(ENABLE_BANNER, String.valueOf(adminAdvisoryBannerDTO.getEnableBanner())));
        resource.setAttributes(attributes);

        ResourceFile file = new ResourceFile();
        file.setName(BANNER_CONTENT);
        try (InputStream inputStream = new ByteArrayInputStream(adminAdvisoryBannerDTO.getBannerContent().
                getBytes(StandardCharsets.UTF_8))) {
            file.setInputStream(inputStream);
        } catch (IOException e) {
            throw new AdminAdvisoryMgtException("Error occurred while converting banner content to input stream.", e);
        }
        List<ResourceFile> resourceFiles = new ArrayList<>();
        resourceFiles.add(file);
        resource.setFiles(resourceFiles);
        return resource;
    }
}
