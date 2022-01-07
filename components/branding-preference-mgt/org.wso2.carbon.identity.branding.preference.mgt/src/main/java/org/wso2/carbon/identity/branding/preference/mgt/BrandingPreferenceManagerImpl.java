/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.branding.preference.mgt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.branding.preference.mgt.exception.BrandingPreferenceMgtException;
import org.wso2.carbon.identity.branding.preference.mgt.internal.BrandingPreferenceManagerComponentDataHolder;
import org.wso2.carbon.identity.branding.preference.mgt.model.BrandingPreference;
import org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtUtils;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.BRANDING_RESOURCE_TYPE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_BRANDING_PREFERENCE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_ADDING_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_BUILDING_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_CHECKING_BRANDING_PREFERENCE_EXISTS;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_DELETING_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_GETTING_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_ERROR_UPDATING_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_INVALID_BRANDING_PREFERENCE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ENCODING_EXCEPTION;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.RESOURCE_ALREADY_EXISTS_ERROR_CODE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.RESOURCE_NAME_SEPARATOR;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtConstants.RESOURCE_NOT_EXISTS_ERROR_CODE;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtUtils.handleClientException;
import static org.wso2.carbon.identity.branding.preference.mgt.util.BrandingPreferenceMgtUtils.handleServerException;

/**
 * Branding Preference Management service implementation.
 */
public class BrandingPreferenceManagerImpl implements BrandingPreferenceManager {

    private static final Log log = LogFactory.getLog(BrandingPreferenceManagerImpl.class);
    //TODO: Improve API to manage application level & language level theming resources in addition to the tenant level.

    @Override
    public BrandingPreference addBrandingPreference(BrandingPreference brandingPreference)
            throws BrandingPreferenceMgtException {

        String resourceName = getResourceName
                (brandingPreference.getType(), brandingPreference.getName(), brandingPreference.getLocale());
        String tenantDomain = getTenantDomain();
        // Check whether a branding resource already exists with the same name in the particular tenant to be added.
        if (isResourceExists(BRANDING_RESOURCE_TYPE, resourceName)) {
            throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_ALREADY_EXISTS, tenantDomain);
        }
        String preferencesJSON = generatePreferencesJSONFromPreference(brandingPreference.getPreference());
        if (!BrandingPreferenceMgtUtils.isValidJSONString(preferencesJSON)) {
            throw handleClientException(ERROR_CODE_INVALID_BRANDING_PREFERENCE, tenantDomain);
        }

        try {
            InputStream inputStream = BrandingPreferenceMgtUtils.generatePreferenceInputStream(preferencesJSON);
            Resource brandingPreferenceResource = buildResourceFromBrandingPreference(resourceName, inputStream);
            getConfigurationManager().addResource(BRANDING_RESOURCE_TYPE, brandingPreferenceResource);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_ALREADY_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("Branding preferences are already exists for tenant: " + tenantDomain, e);
                }
                throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
            }
            throw handleServerException(ERROR_CODE_ERROR_ADDING_BRANDING_PREFERENCE, tenantDomain, e);
        } catch (UnsupportedEncodingException e) {
            throw handleServerException(ERROR_CODE_UNSUPPORTED_ENCODING_EXCEPTION, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Branding preference for tenant: " + tenantDomain + " added successfully");
        }
        return brandingPreference;
    }

    @Override
    public BrandingPreference getBrandingPreference(String type, String name, String locale)
            throws BrandingPreferenceMgtException {

        String resourceName = getResourceName(type, name, locale);
        String tenantDomain = getTenantDomain();
        try {
            // Return default branding preference.
            List<ResourceFile> resourceFiles = getConfigurationManager().getFiles(BRANDING_RESOURCE_TYPE, resourceName);
            if (resourceFiles.isEmpty()) {
                throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
            }
            if (StringUtils.isBlank(resourceFiles.get(0).getId())) {
                throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
            }

            InputStream inputStream = getConfigurationManager().getFileById
                    (BRANDING_RESOURCE_TYPE, resourceName, resourceFiles.get(0).getId());
            if (inputStream == null) {
                throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
            }
            if (log.isDebugEnabled()) {
                log.debug("Branding preference for tenant: " + tenantDomain + " is retrieved successfully.");
            }
            return buildBrandingPreferenceFromResource(inputStream, type, name, locale);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("Can not find a branding preference configurations for tenant: " + tenantDomain, e);
                }
                throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
            }
            throw handleServerException(ERROR_CODE_ERROR_GETTING_BRANDING_PREFERENCE, tenantDomain, e);
        } catch (IOException e) {
            throw handleServerException(ERROR_CODE_ERROR_BUILDING_BRANDING_PREFERENCE, tenantDomain);
        }
    }

    @Override
    public BrandingPreference replaceBrandingPreference(BrandingPreference brandingPreference)
            throws BrandingPreferenceMgtException {

        String resourceName = getResourceName
                (brandingPreference.getType(), brandingPreference.getName(), brandingPreference.getLocale());
        String tenantDomain = getTenantDomain();
        // Check whether the branding resource exists in the particular tenant.
        if (!isResourceExists(BRANDING_RESOURCE_TYPE, resourceName)) {
            throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
        }

        String preferencesJSON = generatePreferencesJSONFromPreference(brandingPreference.getPreference());
        if (!BrandingPreferenceMgtUtils.isValidJSONString(preferencesJSON)) {
            throw handleClientException(ERROR_CODE_INVALID_BRANDING_PREFERENCE, tenantDomain);
        }

        try {
            InputStream inputStream = BrandingPreferenceMgtUtils.generatePreferenceInputStream(preferencesJSON);
            Resource brandingPreferenceResource = buildResourceFromBrandingPreference(resourceName, inputStream);
            getConfigurationManager().replaceResource(BRANDING_RESOURCE_TYPE, brandingPreferenceResource);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_ERROR_UPDATING_BRANDING_PREFERENCE, tenantDomain, e);
        } catch (UnsupportedEncodingException e) {
            throw handleServerException(ERROR_CODE_UNSUPPORTED_ENCODING_EXCEPTION, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Branding preference for tenant: " + tenantDomain + " replaced successfully.");
        }
        return brandingPreference;
    }

    @Override
    public void deleteBrandingPreference(String type, String name, String locale)
            throws BrandingPreferenceMgtException {

        String resourceName = getResourceName(type, name, locale);
        String tenantDomain = getTenantDomain();
        // Check whether the branding resource exists in the particular tenant.
        if (!isResourceExists(BRANDING_RESOURCE_TYPE, resourceName)) {
            throw handleClientException(ERROR_CODE_BRANDING_PREFERENCE_NOT_EXISTS, tenantDomain);
        }

        try {
            getConfigurationManager().deleteResource(BRANDING_RESOURCE_TYPE, resourceName);
        } catch (ConfigurationManagementException e) {
            throw handleServerException(ERROR_CODE_ERROR_DELETING_BRANDING_PREFERENCE, tenantDomain);
        }
        if (log.isDebugEnabled()) {
            log.debug("Branding preference for tenant: " + tenantDomain + " replaced successfully.");
        }
    }

    /**
     * Check whether a branding preference resource already exists with the same name in the particular tenant.
     *
     * @param resourceType Resource type.
     * @param resourceName Resource name.
     * @return Return true if the resource already exists. If not return false.
     */
    private boolean isResourceExists(String resourceType, String resourceName)
            throws BrandingPreferenceMgtException {

        Resource resource;
        try {
            resource = getConfigurationManager().getResource(resourceType, resourceName);
        } catch (ConfigurationManagementException e) {
            if (RESOURCE_NOT_EXISTS_ERROR_CODE.equals(e.getErrorCode())) {
                return false;
            }
            throw handleServerException(ERROR_CODE_ERROR_CHECKING_BRANDING_PREFERENCE_EXISTS, e);
        }
        if (resource == null) {
            return false;
        }
        return true;
    }

    /**
     * Build a JSON string which contains preferences from a preference object.
     *
     * @param object Preference object of Branding Preference Model.
     * @return JSON string which contains preferences.
     */
    private String generatePreferencesJSONFromPreference(Object object) {

        ObjectMapper mapper = new ObjectMapper();
        String preferencesJSON = null;
        try {
            preferencesJSON = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while generating JSON string from the branding preference request.", e);
            }
        }
        return preferencesJSON;
    }

    /**
     * Build a resource object from Branding Preference Model.
     *
     * @param resourceName Branding preference resource name.
     * @param inputStream  Branding preference file stream.
     * @return Resource object.
     */
    private Resource buildResourceFromBrandingPreference(String resourceName, InputStream inputStream) {

        Resource resource = new Resource();
        resource.setResourceName(resourceName);
        // Set file.
        ResourceFile file = new ResourceFile();
        file.setName(resourceName);
        file.setInputStream(inputStream);
        List<ResourceFile> resourceFiles = new ArrayList<>();
        resourceFiles.add(file);
        resource.setFiles(resourceFiles);
        return resource;
    }

    /**
     * Build a Branding Preference Model from branding preference file stream.
     *
     * @param inputStream Branding Preference file stream.
     * @param type        Branding resource type.
     * @param name        Tenant/Application name.
     * @param locale      Language preference
     * @return Branding Preference.
     */
    private BrandingPreference buildBrandingPreferenceFromResource(InputStream inputStream, String type,
                                                                   String name, String locale)
            throws IOException, BrandingPreferenceMgtException {

        String preferencesJSON = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        if (!BrandingPreferenceMgtUtils.isValidJSONString(preferencesJSON)) {
            throw handleServerException(ERROR_CODE_ERROR_BUILDING_BRANDING_PREFERENCE, name);
        }

        ObjectMapper mapper = new ObjectMapper();
        Object preference = mapper.readValue(preferencesJSON, Object.class);
        BrandingPreference brandingPreference = new BrandingPreference();
        brandingPreference.setPreference(preference);
        brandingPreference.setType(type);
        brandingPreference.setName(name);
        brandingPreference.setLocale(locale);
        return brandingPreference;
    }

    /**
     * Generate and return resource name of the branding resource.
     *
     * @param type   Branding resource type.
     * @param name   Tenant/Application name.
     * @param locale Language preference
     * @return resource name of the branding resource.
     */
    private String getResourceName(String type, String name, String locale) {

        /*
          Currently, this API provides the support to only configure tenant wise branding preference for 'en-US' locale.
          So always use resource name as default resource name.
          Default resource name is the name used to save organization level branding for 'en-US' language.
         */
        String resourceName = getTenantId() + RESOURCE_NAME_SEPARATOR + locale;
        return resourceName;
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private ConfigurationManager getConfigurationManager() {

        return BrandingPreferenceManagerComponentDataHolder.getInstance().getConfigurationManager();
    }
}
