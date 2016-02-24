/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provider.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.wso2.carbon.identity.provider.openid.cache.OpenIDAssociationCache;
import org.wso2.carbon.identity.provider.openid.dao.OpenIDAssociationDAO;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * This is the custom AssociationStore. Uses super's methods to generate
 * associations. However this class persist the associations in the identity
 * database. In the case of loading an association it will first look in the
 * super and if fails, it will look in the database. The database may be shared
 * in a clustered environment.
 *
 * @author WSO2 Inc.
 */
public class OpenIDServerAssociationStore extends InMemoryServerAssociationStore {

    private static final Log log = LogFactory.getLog(OpenIDServerAssociationStore.class);
    private static final String SHA_1_PRNG = "SHA1PRNG";
    private int storeId = 0;
    private String timestamp;
    private int counter;
    private volatile OpenIDAssociationCache cache;
    private OpenIDAssociationDAO dao;

    /**
     * Here we instantiate a DAO to access the identity database.
     *
     * @param dbConnection
     * @param privateAssociations if this association store stores private associations
     */
    public OpenIDServerAssociationStore(String associationsType) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SHA_1_PRNG);
            storeId = secureRandom.nextInt(9999);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1PRNG algorithm could not be found.", e);
        }
        timestamp = Long.toString(new Date().getTime());
        counter = 0;
        cache = OpenIDAssociationCache.getCacheInstance();
        // get singleton dao
        dao = OpenIDAssociationDAO.getInstance(associationsType);
    }

    /**
     * Super will generate the association and it will be persisted by the DAO.
     *
     * @param type     association type defined in the OpenID 2.0
     * @param expiryIn date
     * @return <code>Association</code>
     */
    @Override
    public Association generate(String type, int expiryIn)
            throws AssociationException {
        String handle = storeId + timestamp + "-" + getCounter();
        final Association association = Association.generate(type, handle, expiryIn);
        cache.addToCache(association);
        // Asynchronous write to database
        Thread thread = new Thread() {
            @Override
            public void run() {
                if(log.isDebugEnabled()) {
                    log.debug("Storing association " + association.getHandle() + " in the database.");
                }
                dao.storeAssociation(association);
            }
        };
        thread.start();
        return association;
    }

    /**
     *
     * @return
     */
    private synchronized int getCounter(){
        return counter++;
    }

    /**
     * First try to load from the memory, in case of failure look in the db.
     *
     * @param handle
     * @return <code>Association<code>
     */
    @Override
    public Association load(String handle) {

        boolean chacheMiss = false;

        // looking in the cache
        Association association = cache.getFromCache(handle);

        // if failed, look in the database
        if (association == null) {
            if(log.isDebugEnabled()) {
                log.debug("Association " + handle + " not found in cache. Loading from the database.");
            }
            association = dao.loadAssociation(handle);
            chacheMiss = true;
        }

        // no association found for the given handle
        if (association == null) {
            if(log.isDebugEnabled()) {
                log.debug("Association " + handle + " not found in the database.");
            }
            return null;
        }

        // if the association is expired
        if (association.hasExpired()) {
            log.warn("Association is expired for handle " + handle);
            remove(handle); // remove only from db
            return null;

        } else if (chacheMiss) {
            // add the missing entry to the cache
            cache.addToCache(association);
        }

        return association;
    }

    /**
     * Removes the association from the memory and db.
     */
    @Override
    public void remove(final String handle) {

        // we are not removing from cache
        // because it will cost a database call
        // for a cache miss. Associations are self validating tokens

        // removing from the database
        Thread thread = new Thread() {
            @Override
            public void run() {
                if(log.isDebugEnabled()) {
                    log.debug("Removing the association" + handle + " from the database");
                }
                dao.removeAssociation(handle);
            }
        };
        thread.start();
    }
}
