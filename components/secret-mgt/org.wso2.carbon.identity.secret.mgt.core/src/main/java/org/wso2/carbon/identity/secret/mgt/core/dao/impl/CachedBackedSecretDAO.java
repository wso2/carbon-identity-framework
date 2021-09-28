/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.secret.mgt.core.cache.SecretByIdCache;
import org.wso2.carbon.identity.secret.mgt.core.cache.SecretByIdCacheKey;
import org.wso2.carbon.identity.secret.mgt.core.cache.SecretByNameCache;
import org.wso2.carbon.identity.secret.mgt.core.cache.SecretByNameCacheKey;
import org.wso2.carbon.identity.secret.mgt.core.cache.SecretCacheEntry;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;

/**
 * This is a wrapper data access object to the default data access object to provide caching functionalities.
 */
public class CachedBackedSecretDAO implements SecretDAO {

    private static final Log log = LogFactory.getLog(CachedBackedSecretDAO.class);
    private final SecretDAO secretDAO;
    private final SecretByIdCache secretByIdCache;
    private final SecretByNameCache secretByNameCache;

    public CachedBackedSecretDAO(SecretDAO secretDAO) {

        this.secretDAO = secretDAO;
        this.secretByIdCache = SecretByIdCache.getInstance();
        this.secretByNameCache = SecretByNameCache.getInstance();
    }

    @Override
    public int getPriority() {

        return 2;
    }

    @Override
    public Secret getSecretByName(String name, SecretType secretType, int tenantId) throws SecretManagementException {

        Secret secret = getSecretFromCacheByName(name, tenantId);
        if (secret != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for secret by it's name. Secret name: %s, Tenant id: "
                        + "%d", name, tenantId);
                log.debug(message);
            }
            return secret;
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for secret by it's name. Secret name: %s, Tenant id: " +
                        "%d", name, tenantId);
                log.debug(message);
            }
            secret = secretDAO.getSecretByName(name, secretType, tenantId);
            addSecretToCache(secret);
        }
        return secret;
    }

    @Override
    public Secret getSecretById(String secretId, int tenantId) throws SecretManagementException {

        Secret secret = getSecretFromCacheById(secretId, tenantId);
        if (secret != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for secret by it's id. Secret id: %s", secretId);
                log.debug(message);
            }
            return secret;
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for secret by it's id. Secret id: %s", secretId);
                log.debug(message);
            }
            secret = secretDAO.getSecretById(secretId, tenantId);
            addSecretToCache(secret);
        }
        return secret;
    }

    @Override
    public List getSecrets(SecretType secretType, int tenantId) throws SecretManagementException {

        return secretDAO.getSecrets(secretType, tenantId);
    }

    @Override
    public void deleteSecretById(String secretId, int tenantId) throws SecretManagementException {

        secretDAO.deleteSecretById(secretId, tenantId);
        deleteCacheBySecretId(secretId, tenantId);
    }

    @Override
    public void deleteSecretByName(String name, String secretTypeId, int tenantId) throws SecretManagementException {

        secretDAO.deleteSecretByName(name, secretTypeId, tenantId);
        deleteCacheBySecretName(name, tenantId);
    }

    @Override
    public void addSecret(Secret secret) throws SecretManagementException {

        secretDAO.addSecret(secret);
        addSecretToCache(secret);
    }

    @Override
    public void replaceSecret(Secret secret) throws SecretManagementException {

        secretDAO.replaceSecret(secret);
        deleteSecretFromCache(secret);
    }

    @Override
    public Secret updateSecretValue(Secret secret, String value) throws SecretManagementException {

        Secret updatedSecret = secretDAO.updateSecretValue(secret, value);
        deleteSecretFromCache(secret);
        return updatedSecret;
    }

    @Override
    public Secret updateSecretDescription(Secret secret, String description) throws SecretManagementException {

        Secret updatedSecret = secretDAO.updateSecretDescription(secret, description);
        deleteSecretFromCache(secret);
        return updatedSecret;
    }

    @Override
    public boolean isExistingSecret(String secretId, int tenantId) throws SecretManagementException {

        Secret secret = getSecretFromCacheById(secretId, tenantId);
        if (secret != null) {
            return true;
        }
        return secretDAO.isExistingSecret(secretId, tenantId);
    }

    @Override
    public void addSecretType(SecretType secretType) throws SecretManagementException {

        secretDAO.addSecretType(secretType);
    }

    @Override
    public void replaceSecretType(SecretType secretType) throws SecretManagementException {

        secretDAO.replaceSecretType(secretType);
    }

    @Override
    public SecretType getSecretTypeByName(String secretTypeName) throws SecretManagementException {

        return secretDAO.getSecretTypeByName(secretTypeName);
    }

    @Override
    public void deleteSecretTypeByName(String secretTypeName) throws SecretManagementException {

        secretDAO.deleteSecretTypeByName(secretTypeName);
    }

    private Secret getSecretFromCacheById(String secretId, int tenantId) {

        SecretByIdCacheKey secretByIdCacheKey = new SecretByIdCacheKey(secretId);
        SecretCacheEntry secretCacheEntry = secretByIdCache.getValueFromCache(secretByIdCacheKey, tenantId);
        if (secretCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from Secret by id cache. Secret id: %s.", secretId);
                log.debug(message);
            }
            return secretCacheEntry.getSecret();
        }
        return null;
    }

    private Secret getSecretFromCacheByName(String secretName, int tenantId) {

        SecretByNameCacheKey secretByNameCacheKey = new SecretByNameCacheKey(secretName);
        SecretCacheEntry secretCacheEntry = secretByNameCache.getValueFromCache(secretByNameCacheKey, tenantId);
        if (secretCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from secret by name cache. Secret id: %s.",
                        secretName);
                log.debug(message);
            }
            return secretCacheEntry.getSecret();
        }
        return null;
    }

    private void addSecretToCache(Secret secret) {

        if (secret == null) {
            return;
        }
        SecretByIdCacheKey secretByIdCacheKey = new SecretByIdCacheKey(secret.getSecretId());
        SecretByNameCacheKey secretByNameCacheKey = new SecretByNameCacheKey(secret.getSecretName());
        SecretCacheEntry secretCacheEntry = new SecretCacheEntry(secret);
        if (log.isDebugEnabled()) {
            String message = String.format("Following two cache entries created. 1. Secret by name cache %s, 2." +
                            " Secret by id cache %s. Tenant domain for all caches: %s", secret.getSecretName(),
                    secret.getSecretId(), secret.getTenantDomain());
            log.debug(message);
        }
        secretByIdCache.addToCache(secretByIdCacheKey, secretCacheEntry, secret.getTenantDomain());
        secretByNameCache.addToCache(secretByNameCacheKey, secretCacheEntry, secret.getTenantDomain());
    }

    private void deleteSecretFromCache(Secret secret) {

        if (secret == null) {
            return;
        }
        SecretByIdCacheKey secretByIdCacheKey = new SecretByIdCacheKey(secret.getSecretId());
        SecretByNameCacheKey secretByNameCacheKey = new SecretByNameCacheKey(secret.getSecretName());

        if (log.isDebugEnabled()) {
            String message = String.format("Following two cache entries deleted. 1. Secret by name cache %s, 2." +
                            " Secret by id cache %s. Tenant domain for all caches: %s", secret.getSecretName(),
                    secret.getSecretId(), secret.getTenantDomain());
            log.debug(message);
        }

        secretByIdCache.clearCacheEntry(secretByIdCacheKey, secret.getTenantDomain());
        secretByNameCache.clearCacheEntry(secretByNameCacheKey, secret.getTenantDomain());
    }

    private void deleteCacheBySecretId(String secretId, int tenantId) throws SecretManagementException {

        Secret secret = getSecretFromCacheById(secretId, tenantId);
        if (secret == null) {
            return;
        }
        deleteSecretFromCache(secret);
    }

    private void deleteCacheBySecretName(String secretName, int tenantId) throws SecretManagementException {

        Secret secret = getSecretFromCacheByName(secretName, tenantId);
        if (secret == null) {
            return;
        }
        deleteSecretFromCache(secret);
    }
}
