/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt.defaultsequence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;

/**
 * Tenant wise default authentication sequence management admin service.
 */
public class DefaultAuthSeqMgtAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(DefaultAuthSeqMgtAdminService.class);
    private DefaultAuthSeqMgtService authenticationSeqMgtService;

    /**
     * Create default authentication sequence.
     *
     * @param authenticationSequence default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public void createDefaultAuthenticationSeq(DefaultAuthenticationSequence authenticationSequence)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            authenticationSeqMgtService.createDefaultAuthenticationSeq(authenticationSequence, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while creating default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq(String sequenceName)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeq(sequenceName, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence in XML.
     *
     * @param sequenceName name of the default authentication sequence
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML(String sequenceName)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeqInXML(sequenceName, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence of tenant: " + getTenantDomain() +
                    " in XML format", e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Check existence of default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @return true if a default authentication sequence exists for the tenant
     * @throws DefaultAuthSeqMgtException
     */
    public boolean isExistingDefaultAuthenticationSequence(String sequenceName)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            return authenticationSeqMgtService.isExistingDefaultAuthenticationSequence(sequenceName, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while checking existence of default authentication sequence in tenant: " +
                    getTenantDomain(), e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Retrieve default authentication sequence basic info.
     *
     * @param sequenceName name of the default authentication sequence
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String sequenceName)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeqInfo(sequenceName, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence info of tenant: " + getTenantDomain(),
                    e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Delete default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public void deleteDefaultAuthenticationSeq(String sequenceName) throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            authenticationSeqMgtService.deleteDefaultAuthenticationSeq(sequenceName, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while deleting default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }

    /**
     * Update default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public void updateDefaultAuthenticationSeq(String sequenceName, DefaultAuthenticationSequence sequence)
            throws DefaultAuthSeqMgtException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
            authenticationSeqMgtService.updateDefaultAuthenticationSeq(sequenceName, sequence, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while updating default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtException("Server error occurred.");
        }
    }
}
