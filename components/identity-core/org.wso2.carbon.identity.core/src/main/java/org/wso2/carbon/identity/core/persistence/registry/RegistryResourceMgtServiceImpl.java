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
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import static org.wso2.carbon.identity.base.IdentityRuntimeException.ErrorInfo.ErrorInfoBuilder;

/**
 * A Util OSGi service to provide registry resource management functionality based on locale
 */
public class RegistryResourceMgtServiceImpl implements RegistryResourceMgtService {

    private static final Log log = LogFactory.getLog(RegistryResourceMgtServiceImpl.class);
    private static final RegistryService registryService = IdentityTenantUtil.getRegistryService();

    private static final String EN_US = "en_us";

    @Override
    public Resource getIdentityResource(String path,
                                        String tenantDomain,
                                        String locale) throws IdentityRuntimeException {

        String localeString = StringUtils.isBlank(locale) ? EN_US : locale.toLowerCase();
        path = path + RegistryConstants.PATH_SEPARATOR + localeString;

        return getIdentityResource(path, tenantDomain);
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path,
                                    String tenantDomain,
                                    String locale) throws IdentityRuntimeException {

        String localeString = StringUtils.isBlank(locale) ? EN_US : locale.toLowerCase();
        path = path + RegistryConstants.PATH_SEPARATOR + localeString;

        putIdentityResource(identityResource, path, tenantDomain);
    }

    @Override
    public Resource getIdentityResource(String path, String tenantDomain) throws IdentityRuntimeException {

        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            Resource resource = null;

            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
            return resource;
        } catch (RegistryException e) {
            String errorMsg = "Error retrieving registry resource of tenant : " + tenantDomain + " from " + path;
            throw getIdentityRunTimeException(errorMsg, e);
        }
    }

    @Override
    public void putIdentityResource(Resource identityResource,
                                    String path, String tenantDomain) throws IdentityRuntimeException {

        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            registry.put(path, identityResource);
            if (log.isDebugEnabled()) {
                log.debug("Resource persisted at " + path + " in " + tenantDomain + " tenant domain.");
            }
        } catch (RegistryException e) {
            String errorMsg = "Error persisting registry resource of tenant : " + tenantDomain + " at " + path;
            throw getIdentityRunTimeException(errorMsg, e);
        }
    }

    @Override
    public void deleteIdentityResource(String path, String tenantDomain) throws IdentityRuntimeException {
        try {
            Registry registry = getRegistryForTenant(tenantDomain);
            if (registry.resourceExists(path)) {
                registry.delete(path);
            } else {
                String errorMsg = "Resource does not exist at " + path + " in " + tenantDomain + "tenant domain.";
                throw getIdentityRunTimeException(errorMsg, null);
            }
        } catch (RegistryException e) {
            String errorMsg = "Error deleting registry resource of tenant : " + tenantDomain + " at " + path;
            throw getIdentityRunTimeException(errorMsg, e);
        }
    }


    /**
     * @param errorDescription
     * @param throwable
     * @return
     */
    private IdentityRuntimeException getIdentityRunTimeException(String errorDescription, Throwable throwable) {
        ErrorInfoBuilder errorInfoBuilder = new ErrorInfoBuilder(errorDescription);
        if (throwable == null) {
            errorInfoBuilder.cause(throwable);
        }
        return IdentityRuntimeException.error(errorInfoBuilder.build());
    }

    /**
     * @param tenantDomain
     * @return
     * @throws RegistryException
     */
    private Registry getRegistryForTenant(String tenantDomain) throws RegistryException {
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return registryService.getConfigSystemRegistry(tenantId);
    }

}
