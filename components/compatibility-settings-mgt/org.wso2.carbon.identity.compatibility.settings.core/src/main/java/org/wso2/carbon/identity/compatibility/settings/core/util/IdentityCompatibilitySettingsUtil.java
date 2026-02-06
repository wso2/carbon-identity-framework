/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.compatibility.settings.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.compatibility.settings.core.cache.CompatibilitySettingCache;
import org.wso2.carbon.identity.compatibility.settings.core.cache.CompatibilitySettingCacheEntry;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.deserializer.InstantDeserializer;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingClientException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.internal.IdentityCompatibilitySettingsDataHolder;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySetting;
import org.wso2.carbon.identity.compatibility.settings.core.model.CompatibilitySettingGroup;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for compatibility settings which provides common utility functionalities.
 */
public class IdentityCompatibilitySettingsUtil {

    private static final Log LOG = LogFactory.getLog(IdentityCompatibilitySettingsUtil.class);

    /**
     * Handle the compatibility setting server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return CompatibilitySettingServerException.
     */
    public static CompatibilitySettingServerException handleServerException(ErrorMessages error, Throwable e,
                                                                             Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new CompatibilitySettingServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the compatibility setting server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return CompatibilitySettingServerException.
     */
    public static CompatibilitySettingServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new CompatibilitySettingServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the compatibility setting client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return CompatibilitySettingClientException.
     */
    public static CompatibilitySettingClientException handleClientException(ErrorMessages error, Throwable e,
                                                                             Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new CompatibilitySettingClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the compatibility setting client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return CompatibilitySettingClientException.
     */
    public static CompatibilitySettingClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new CompatibilitySettingClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Parse compatibility settings from a json file and convert to CompatibilitySettingMetaData model.
     *
     * @param filePath The path to the compatibility settings file.
     * @return CompatibilitySettingMetaData parsed from the file.
     * @throws IOException            If an error occurs while reading the file.
     * @throws DateTimeParseException If an error occurs while parsing date time values in the
     */
    public static CompatibilitySettingMetaData parseCompatibilitySettingsFromJSONFile(String filePath)
            throws IOException, DateTimeParseException {

        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Instant.class, new InstantDeserializer())
                    .create();

            Type type = new TypeToken<Map<String, Map<String, CompatibilitySettingMetaDataEntry>>>() { }.getType();
            Map<String, Map<String, CompatibilitySettingMetaDataEntry>> rawMetaData = gson.fromJson(reader, type);

            if (rawMetaData == null) {
                return new CompatibilitySettingMetaData();
            }
            return convertToCompatibilitySettingMetaData(rawMetaData);
        }
    }

    /**
     * Build a CompatibilitySettingGroup object from a Configuration Resource.
     *
     * @param resource     Configuration resource.
     * @param settingGroup Setting group name.
     * @return CompatibilitySetting object, or null if resource has no attributes.
     */
    public static CompatibilitySettingGroup buildCompatibilitySettingGroupFromResource(Resource resource,
                                                                                       String settingGroup) {

        if (resource == null) {
            return null;
        }

        CompatibilitySettingGroup compatibilitySettingGroup = new CompatibilitySettingGroup();
        compatibilitySettingGroup.setSettingGroup(settingGroup);

        List<Attribute> attributes = resource.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            for (Attribute attribute : attributes) {
                if (StringUtils.isNotBlank(attribute.getKey())) {
                    compatibilitySettingGroup.addSetting(attribute.getKey(), attribute.getValue());
                }
            }
        }

        return compatibilitySettingGroup;
    }

    /**
     * Build a Resource object from a CompatibilitySettingGroup for Configuration Store.
     *
     * @param resourceType               Resource type name.
     * @param resourceName               Resource name.
     * @param compatibilitySettingGroup  The compatibility setting group.
     * @return Resource object with attributes from the compatibility setting group.
     */
    public static Resource buildResourceFromCompatibilitySettingGroup(String resourceType, String resourceName,
                                                                      CompatibilitySettingGroup
                                                                              compatibilitySettingGroup) {

        Resource resource = new Resource();
        resource.setResourceName(resourceName);
        resource.setResourceType(resourceType);

        List<Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, String> entry : compatibilitySettingGroup.getSettings().entrySet()) {
            attributes.add(new Attribute(entry.getKey(), entry.getValue()));
        }
        resource.setAttributes(attributes);
        return resource;
    }

    /**
     * Get the organization creation time for a given tenant domain.
     *
     * @param tenantDomain        The tenant domain.
     * @param enableInheritance   If true, returns the root organization's (greatest parent) creation time.
     *                            If false, returns the organization's own creation time.
     * @return The organization creation time as Instant, or null if not found or on error.
     */
    public static Instant getOrganizationCreationTime(String tenantDomain, boolean enableInheritance)
            throws OrganizationManagementException {

        if (StringUtils.isBlank(tenantDomain)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Tenant domain is empty. Organization creation time retrieval failed.");
            }
            return null;
        }

        OrganizationManager organizationManager =
               IdentityCompatibilitySettingsDataHolder.getInstance().getOrganizationManager();

        if (organizationManager == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("OrganizationManager is not available. Organization creation time retrieval failed.");
            }
            return null;
        }

        String organizationId = organizationManager.resolveOrganizationId(tenantDomain);

        if (StringUtils.isBlank(organizationId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization ID not found for tenant domain: " + tenantDomain);
            }
            return null;
        }

        Organization organization =
                organizationManager.getOrganization(organizationId, false, false);

        if (organization == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization not found for tenant domain: " + tenantDomain);
            }
            return null;
        }

        if (enableInheritance && organization.getParent() != null) {
            String parentTenantDomain =
                    OrganizationManagementUtil.getRootOrgTenantDomainBySubOrgTenantDomain(tenantDomain);
            try {
                // Start a tenant flow as the root org to access parent organization details since organizational
                // manager does no support accessing super organization details in a sub-organization tenant flow.
                FrameworkUtils.startTenantFlow(parentTenantDomain);
                String rootOrgId = organizationManager.resolveOrganizationId(parentTenantDomain);
                if (rootOrgId != null && !rootOrgId.equals(organization.getId())) {
                    Organization rootorganization =
                            organizationManager.getOrganization(rootOrgId, false, false);
                    if (rootorganization != null) {
                        return rootorganization.getCreated();
                    }
                }
            } finally {
                FrameworkUtils.endTenantFlow();
            }

        }

        if (organization.getCreated() != null) {
            return organization.getCreated();
        }
        return null;
    }

    /**
     * Convert raw metadata map to CompatibilitySettingMetaData model.
     *
     * @param rawMetaData Raw metadata map from JSON.
     * @return CompatibilitySettingMetaData model.
     */
    private static CompatibilitySettingMetaData convertToCompatibilitySettingMetaData(
            Map<String, Map<String, CompatibilitySettingMetaDataEntry>> rawMetaData) {

        CompatibilitySettingMetaData compatibilitySettingMetaData = new CompatibilitySettingMetaData();
        rawMetaData.forEach((settingGroup, entries) -> {
            CompatibilitySettingMetaDataGroup metaDataGroup = new CompatibilitySettingMetaDataGroup();
            metaDataGroup.setSettingGroup(settingGroup);
            entries.forEach(metaDataGroup::addSettingMetaData);
            compatibilitySettingMetaData.addSettingMetaDataGroup(settingGroup, metaDataGroup);
        });

        return compatibilitySettingMetaData;
    }


    /**
     * Add compatibility setting to cache for a tenant.
     *
     * @param tenantDomain        Tenant domain.
     * @param compatibilitySetting Compatibility setting to cache.
     */
    public static void addToCache(String tenantDomain, CompatibilitySetting compatibilitySetting) {

        if (StringUtils.isBlank(tenantDomain) || compatibilitySetting == null) {
            return;
        }
        CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(compatibilitySetting);
        CompatibilitySettingCache.getInstance().addToCache(tenantDomain, cacheEntry);
    }

    /**
     * Get compatibility setting from cache for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return Compatibility setting, or null if not found in cache.
     */
    public static CompatibilitySetting getFromCache(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return null;
        }
        CompatibilitySettingCacheEntry cacheEntry =
                CompatibilitySettingCache.getInstance().getFromCache(tenantDomain);
        if (cacheEntry != null) {
            return cacheEntry.getCompatibilitySetting();
        }
        return null;
    }

    /**
     * Get compatibility setting group from cache for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @param settingGroup Setting group name.
     * @return Compatibility setting containing cached group, or null if not found in cache.
     */
    public static CompatibilitySetting getFromCache(String tenantDomain, String settingGroup) {

        if (StringUtils.isBlank(tenantDomain) || StringUtils.isBlank(settingGroup)) {
            return null;
        }
        CompatibilitySettingCacheEntry cacheEntry =
                CompatibilitySettingCache.getInstance().getFromCache(tenantDomain);
        if (cacheEntry != null) {
            CompatibilitySetting cachedSetting = cacheEntry.getCompatibilitySetting();
            if (cachedSetting != null) {
                CompatibilitySettingGroup cachedGroup = cachedSetting.getCompatibilitySettings().get(settingGroup);
                if (cachedGroup != null) {
                    CompatibilitySetting result = new CompatibilitySetting();
                    result.addCompatibilitySetting(cachedGroup);
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Update compatibility setting in cache for a tenant.
     *
     * @param tenantDomain        Tenant domain.
     * @param compatibilitySetting Updated compatibility setting.
     */
    public static void updateCache(String tenantDomain, CompatibilitySetting compatibilitySetting) {

        if (StringUtils.isBlank(tenantDomain) || compatibilitySetting == null) {
            return;
        }
        CompatibilitySetting existingSetting = getFromCache(tenantDomain);
        if (existingSetting != null) {
            existingSetting.updateCompatibilitySetting(compatibilitySetting);
            CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(existingSetting);
            // Note {@link CompatibilitySettingCache#updateCache} clears the existing cache entry and adds the new one.
            CompatibilitySettingCache.getInstance().updateCache(tenantDomain, cacheEntry);
        } else {
            CompatibilitySettingCacheEntry cacheEntry = new CompatibilitySettingCacheEntry(compatibilitySetting);
            CompatibilitySettingCache.getInstance().addToCache(tenantDomain, cacheEntry);
        }
    }

    /**
     * Clear compatibility setting from cache for a tenant.
     *
     * @param tenantDomain Tenant domain.
     */
    public static void clearCache(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }
        CompatibilitySettingCache.getInstance().clearFromCache(tenantDomain);
    }
}

