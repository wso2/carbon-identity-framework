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
import org.wso2.carbon.identity.core.model.ParameterDO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class ParameterDAO extends AbstractDAO<ParameterDO> {

    protected Log log = LogFactory.getLog(ParameterDAO.class);

    /**
     * @param registry
     */
    public ParameterDAO(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param parameterDO
     * @throws IdentityException
     */
    public void createOrUpdateParameter(ParameterDO parameterDO) throws IdentityException {
        String path = null;
        Resource resource = null;

        if (log.isDebugEnabled()) {
            log.debug("Creating or updating parameter");
        }

        try {
            path = IdentityRegistryResources.CARD_ISSUER;
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newResource();
            }

            if (resource.getProperty(parameterDO.getName()) != null) {
                resource.removeProperty(parameterDO.getName());
            }

            resource.addProperty(parameterDO.getName(), parameterDO.getValue());
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Error while creating or updating parameter", e);
            throw IdentityException.error("Error while creating or updating parameter", e);
        }
    }

    /**
     * @param paramName
     * @return
     * @throws IdentityException
     */
    public ParameterDO getParameter(String paramName) throws IdentityException {
        ParameterDO param = null;
        String path = null;
        Resource resource = null;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving parameter " + paramName);
        }

        try {
            path = IdentityRegistryResources.CARD_ISSUER;
            param = new ParameterDO();
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
                if (resource != null) {
                    param.setName(paramName);
                    param.setValue(resource.getProperty(paramName));
                }
            }
        } catch (RegistryException e) {
            log.error("Error while retrieving parameter " + paramName, e);
            throw IdentityException.error("Error while retrieving parameter " + paramName, e);
        }
        return param;
    }

    /**
     * @param parameterDO
     * @throws IdentityException
     */
    public void removeParameter(ParameterDO parameterDO) throws IdentityException {
        String path = null;
        Resource resource = null;

        if (log.isDebugEnabled()) {
            log.debug("Removing parameter");
        }

        try {
            path = IdentityRegistryResources.CARD_ISSUER;
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
                if (resource != null) {
                    resource.removeProperty(parameterDO.getName());
                    registry.put(path, resource);
                }
            }
        } catch (RegistryException e) {
            log.error("Error while removing parameter", e);
            throw IdentityException.error("Error while removing parameter", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected ParameterDO resourceToObject(Resource resource) {
        // TODO Auto-generated method stub
        return null;
    }

}
