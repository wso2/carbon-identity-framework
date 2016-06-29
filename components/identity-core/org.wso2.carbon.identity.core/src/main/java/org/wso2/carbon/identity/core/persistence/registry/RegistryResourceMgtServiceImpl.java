/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.persistence.registry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import static org.wso2.carbon.identity.base.IdentityRuntimeException.ErrorInfo.ErrorInfoBuilder;

/**
 * A Util OSGi service to exposes Registry resource management functionality based on locale.
 */
public class RegistryResourceMgtServiceImpl implements RegistryResourceMgtService {

    private static final Log log = LogFactory.getLog(RegistryResourceMgtServiceImpl.class);
    private static final RegistryService registryService = IdentityTenantUtil.getRegistryService();

    private static final String EN_US = "en_us";
    private static final String BLACKLIST_REGEX = ".*[/\\\\<>`\"].*";

    @Override
    public Resource getIdentityResource(String path,
                                        String tenantDomain,
                                        String locale) throws IdentityRuntimeException {
        locale = validateLocale(locale);
        path = getRegistryPath(path, locale);
        return getIdentityResource(path, tenantDomain);
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain,
                                    String locale) throws IdentityRuntimeException {
        locale = validateLocale(locale);
        path = getRegistryPath(path, locale);
        putIdentityResource(identityResource, path, tenantDomain);
    }

    @Override
    public void deleteIdentityResource(String path,
                                       String tenantDomain,
                                       String locale) throws IdentityRuntimeException {
        locale = validateLocale(locale);
        path = getRegistryPath(path, locale);
        deleteIdentityResource(path, tenantDomain);
    }

    @Override
    public Resource getIdentityResource(String path,
                                        String tenantDomain) throws IdentityRuntimeException {
        startTenantFlow(tenantDomain);
        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            Resource resource = null;

            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
            return resource;
        } catch (RegistryException e) {
            String errorMsg = String.format(
                    "Error retrieving registry resource from %s for tenant %s.", path, tenantDomain);
            throw getIdentityRunTimeException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain) throws IdentityRuntimeException {
        startTenantFlow(tenantDomain);
        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            registry.put(path, identityResource);
            if (log.isDebugEnabled()) {
                String msg = String.format(
                        "Resource persisted at %s in %s tenant domain registry.", path, tenantDomain);
                log.debug(msg);
            }
        } catch (RegistryException e) {
            String errorMsg = String.format(
                    "Error persisting registry resource of %s tenant at %s", tenantDomain, path);
            log.error(errorMsg);
            throw getIdentityRunTimeException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void deleteIdentityResource(String path, String tenantDomain) throws IdentityRuntimeException {
        startTenantFlow(tenantDomain);
        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            if (registry.resourceExists(path)) {
                registry.delete(path);
            } else {
                String errorMsg = String.format(
                        "Resource does not exist at %s in %s tenant domain.", path, tenantDomain);
                log.error(errorMsg);
                throw getIdentityRunTimeException(errorMsg, null);
            }
        } catch (RegistryException e) {
            String errorMsg = String.format(
                    "Error deleting registry resource of tenant : %s at %s.", tenantDomain, path);
            throw getIdentityRunTimeException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private IdentityRuntimeException getIdentityRunTimeException(String errorDescription, Throwable throwable) {
        ErrorInfoBuilder errorInfoBuilder = new ErrorInfoBuilder(errorDescription);
        if (throwable != null) {
            errorInfoBuilder.cause(throwable);
        }
        return IdentityRuntimeException.error(errorInfoBuilder.build());
    }

    private Registry getRegistryForTenant(String tenantDomain) throws RegistryException {
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return registryService.getConfigSystemRegistry(tenantId);
    }

    /**
     * Get the registry path for the resource based on it's locale. Here we follow a convention that the leaf element
     * of the path is the resource and we name the resource by it's locale value. We can derive the type of the resource
     * using the path. ( eg: /identity/challengeQuestions/Set1/question1/en_us)
     *
     * @param path   Path to the resource relative to the root of the configuration registry to the parent directory of
     *               the resource
     * @param locale locale of the resource which will also be the name of the resource.
     * @return
     */
    private String getRegistryPath(String path, String locale) {
        path = path + RegistryConstants.PATH_SEPARATOR + locale;
        return path;
    }

    private void startTenantFlow(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    /**
     * Validate whether the provided locale string contains invalid characters. If and empty or null string is provided
     * we consider the locale as the default locale en_US
     *
     * @param locale
     * @return
     * @throws IllegalArgumentException
     */
    private String validateLocale(String locale) throws IllegalArgumentException {
        String localeString = StringUtils.isBlank(locale) ? EN_US : locale.toLowerCase();

        if (localeString.matches(BLACKLIST_REGEX)) {
            throw new IllegalArgumentException("Locale contains invalid special characters : " + locale);
        }

        return localeString;
    }

}
