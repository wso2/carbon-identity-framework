/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.core.model.OpenIDAdminDO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class OpenIDAdminDAO extends AbstractDAO<OpenIDAdminDO> {

    protected Log log = LogFactory.getLog(OpenIDAdminDAO.class);

    /**
     * @param registry
     */
    public OpenIDAdminDAO(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param rp
     * @throws IdentityException
     */
    public void createOrUpdate(OpenIDAdminDO opAdmin) throws IdentityException {
        String path = null;
        Resource resource = null;

        try {
            path = IdentityRegistryResources.OPEN_ID_ADMIN_SETTINGS;
            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating new openid admin");
                }
                resource = registry.newResource();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Updating openid admin");
                }
                resource = registry.get(path);
                resource.removeProperty(IdentityRegistryResources.SUB_DOMAIN);
                resource.removeProperty(IdentityRegistryResources.OPENID_PATTERN);
            }
            resource.addProperty(IdentityRegistryResources.SUB_DOMAIN, opAdmin.getSubDomain());
            resource.addProperty(IdentityRegistryResources.OPENID_PATTERN, opAdmin
                    .getTenantOpenIDPattern());
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Error while creating/updating openid admin", e);
            throw IdentityException.error("Error while creating/updating openid admin", e);
        }
    }

    /**
     * @param hostName
     * @return
     * @throws IdentityException
     */
    public OpenIDAdminDO getOpenIDAdminDO() throws IdentityException {
        OpenIDAdminDO opdo = null;
        Resource resource = null;

        if (log.isDebugEnabled()) {
            log.debug("Retreiving OpenID admin for tenant");
        }
        try {
            if (registry.resourceExists(IdentityRegistryResources.OPEN_ID_ADMIN_SETTINGS)) {
                resource = registry.get(IdentityRegistryResources.OPEN_ID_ADMIN_SETTINGS);
                return resourceToObject(resource);
            }
        } catch (RegistryException e) {
            log.error("Error while retreiving openid admin", e);
            throw IdentityException.error("Error while retreiving openid admin", e);
        }
        return opdo;
    }

    /**
     * {@inheritDoc}
     */
    protected OpenIDAdminDO resourceToObject(Resource resource) {
        OpenIDAdminDO opdo = null;
        if (resource != null) {
            opdo = new OpenIDAdminDO();
            String subDomain = resource.getProperty(IdentityRegistryResources.SUB_DOMAIN);
            String openIDPattern = resource.getProperty(IdentityRegistryResources.OPENID_PATTERN);
            opdo.setSubDomain(subDomain);
            opdo.setTenantOpenIDPattern(openIDPattern);
        }
        return opdo;
    }
}
