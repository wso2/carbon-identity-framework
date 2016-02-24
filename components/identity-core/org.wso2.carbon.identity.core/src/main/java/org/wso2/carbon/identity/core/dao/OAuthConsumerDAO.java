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
import org.wso2.carbon.identity.core.model.OAuthConsumerDO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

public class OAuthConsumerDAO extends AbstractDAO<OAuthConsumerDO> {

    protected Log log = LogFactory.getLog(OAuthConsumerDAO.class);

    /**
     * @param registry
     */
    public OAuthConsumerDAO(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param ppid
     * @throws IdentityException
     */
    public void registerOAuthConsumer(OAuthConsumerDO consumer) throws IdentityException {

        Collection userResource = null;

        if (log.isDebugEnabled()) {
            log.debug("Creating or updating OAuth consumer value");
        }

        try {

            boolean transactionStarted = Transaction.isStarted();
            try {
                if (!transactionStarted) {
                    registry.beginTransaction();
                }

                if (!registry.resourceExists(RegistryConstants.PROFILES_PATH
                        + consumer.getConsumerKey())) {
                    userResource = registry.newCollection();
                    registry.put(RegistryConstants.PROFILES_PATH + consumer.getConsumerKey(),
                            userResource);
                } else {
                    userResource = (Collection) registry.get(RegistryConstants.PROFILES_PATH
                            + consumer.getConsumerKey());
                    userResource.removeProperty(IdentityRegistryResources.OAUTH_CONSUMER_PATH);
                }

                userResource.addProperty(IdentityRegistryResources.OAUTH_CONSUMER_PATH, consumer
                        .getConsumerSecret());

                registry.put(RegistryConstants.PROFILES_PATH + consumer.getConsumerKey(),
                        userResource);
                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                if (!transactionStarted) {
                    registry.rollbackTransaction();
                }
                if (e instanceof RegistryException) {
                    throw (RegistryException) e;
                } else {
                    throw IdentityException.error("Error while creating or updating OAuth consumer",
                            e);
                }
            }
        } catch (RegistryException e) {
            log.error("Error while creating or updating OAuth consumer", e);
            throw IdentityException.error("Error while creating or updating OAuth consumer", e);
        }
    }

    /**
     * @param ppid
     * @return
     * @throws IdentityException
     */
    public String getOAuthConsumerSecret(String consumerKey) throws IdentityException {
        String path = null;
        Resource resource = null;

        if (log.isDebugEnabled()) {
            log.debug("Retreiving user for OAuth consumer key  " + consumerKey);
        }

        try {
            path = RegistryConstants.PROFILES_PATH + consumerKey;
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
                return resource.getProperty(IdentityRegistryResources.OAUTH_CONSUMER_PATH);
            } else {
                return null;
            }
        } catch (RegistryException e) {
            log.error("Error while retreiving user for OAuth consumer key  " + consumerKey, e);
            throw IdentityException.error("Error while retreiving user for OAuth consumer key  "
                    + consumerKey, e);
        }
    }

    @Override
    protected OAuthConsumerDO resourceToObject(Resource resource) {
        // TODO Auto-generated method stub
        return null;
    }
}