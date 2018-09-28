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
     * @throws DefaultAuthSeqMgtClientException
     */
    public void createDefaultAuthenticationSeq(DefaultAuthenticationSequence authenticationSequence)
            throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            authenticationSeqMgtService.createDefaultAuthenticationSeq(authenticationSequence, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while creating default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }

    /**
     * Retrieve default authentication sequence.
     *
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtClientException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeq() throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeq(getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }

    /**
     * Retrieve default authentication sequence in XML.
     *
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtClientException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML() throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeqInXML(getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence of tenant: " + getTenantDomain() +
                    " in XML format", e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }


    /**
     * Retrieve default authentication sequence basic info.
     *
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    public DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo()
            throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            return authenticationSeqMgtService.getDefaultAuthenticationSeqInfo(getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while retrieving default authentication sequence info of tenant: " + getTenantDomain(),
                    e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }

    /**
     * Delete default authentication sequence.
     *
     * @throws DefaultAuthSeqMgtClientException
     */
    public void deleteDefaultAuthenticationSeq() throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            authenticationSeqMgtService.deleteDefaultAuthenticationSeq(getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while deleting default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }

    /**
     * Update default authentication sequence.
     *
     * @throws DefaultAuthSeqMgtClientException
     */
    public void updateDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence)
            throws DefaultAuthSeqMgtClientException {

        try {
            authenticationSeqMgtService = DefaultAuthSeqMgtService.getInstance();
            authenticationSeqMgtService.updateDefaultAuthenticationSeq(sequence, getTenantDomain());
        } catch (DefaultAuthSeqMgtServerException e) {
            log.error("Error while updating default authentication sequence of tenant: " + getTenantDomain(), e);
            throw new DefaultAuthSeqMgtClientException("Server error occurred.");
        } catch (DefaultAuthSeqMgtException e) {
            throw (DefaultAuthSeqMgtClientException) e;
        }
    }
}
