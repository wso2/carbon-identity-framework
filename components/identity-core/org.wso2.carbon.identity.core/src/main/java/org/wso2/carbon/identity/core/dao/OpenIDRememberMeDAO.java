/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OpenIDRememberMeDO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

public class OpenIDRememberMeDAO extends AbstractDAO<OpenIDRememberMeDO> {

    protected Log log = LogFactory.getLog(OpenIDRememberMeDAO.class);

    public OpenIDRememberMeDAO(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param rememberMe
     * @throws IdentityException
     */
    public void updateToken(OpenIDRememberMeDO rememberMe) throws IdentityException {
        Collection userResource = null;
        boolean transactionStarted = Transaction.isStarted();

        try {
            if (!registry.resourceExists(RegistryConstants.PROFILES_PATH + rememberMe.getUserName())) {
                userResource = registry.newCollection();
                registry.put(RegistryConstants.PROFILES_PATH + rememberMe.getUserName(),
                        userResource);
            } else {
                userResource =
                        (Collection) registry.get(RegistryConstants.PROFILES_PATH +
                                rememberMe.getUserName());
            }

            if (!transactionStarted) {
                registry.beginTransaction();
            }

            userResource.removeProperty("OpenIDRememberMeToken");
            userResource.addProperty("OpenIDRememberMeToken", rememberMe.getToken());
            registry.put(RegistryConstants.PROFILES_PATH + rememberMe.getUserName(), userResource);

            if (!transactionStarted) {
                registry.commitTransaction();
            }

        } catch (Exception ex) {
            if (!transactionStarted) {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    log.error("Error occured while updating OpenID remember me token", e);
                    throw IdentityException.error(
                            "Error occured while updating OpenID remember me token",
                            e);
                }
            }
        }
    }

    /**
     * @param rememberMe
     * @return
     * @throws IdentityException
     */
    public String getToken(OpenIDRememberMeDO rememberMe) throws IdentityException {
        Collection userResource = null;
        String value = null;

        try {
            if (!registry.resourceExists(RegistryConstants.PROFILES_PATH + rememberMe.getUserName())) {
                return null;
            } else {
                userResource =
                        (Collection) registry.get(RegistryConstants.PROFILES_PATH +
                                rememberMe.getUserName());
            }

            value = userResource.getProperty("OpenIDRememberMeToken");

        } catch (Exception e) {
            log.error("Error occured while updating OpenID remember me token", e);
            throw IdentityException.error("Error occured while updating OpenID remember me token", e);
        }

        return value;
    }

    @Override
    protected OpenIDRememberMeDO resourceToObject(Resource resource) {
        // TODO Auto-generated method stub
        return null;
    }

}
