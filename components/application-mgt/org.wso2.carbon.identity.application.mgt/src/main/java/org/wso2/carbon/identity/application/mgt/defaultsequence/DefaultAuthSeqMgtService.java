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

import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;

/**
 * Tenant wise default authentication sequence management service abstract class.
 */
public abstract class DefaultAuthSeqMgtService {

    /**
     * Get DefaultAuthSeqMgtService instance.
     *
     * @return DefaultAuthSeqMgtService instance
     */
    public static DefaultAuthSeqMgtService getInstance() {

        return DefaultAuthSeqMgtServiceImpl.getInstance();
    }

    /**
     * Create default authentication sequence.
     *
     * @param authenticationSequence default authentication sequence
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtException
     */
    public abstract void createDefaultAuthenticationSeq(DefaultAuthenticationSequence authenticationSequence,
                                                        String tenantDomain) throws DefaultAuthSeqMgtException;

    /**
     * Retrieve default authentication sequence.
     *
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public abstract DefaultAuthenticationSequence getDefaultAuthenticationSeq(String tenantDomain)
            throws DefaultAuthSeqMgtException;

    /**
     * Retrieve default authentication sequence in XML format.
     *
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtException
     */
    public abstract DefaultAuthenticationSequence getDefaultAuthenticationSeqInXML(String tenantDomain)
            throws DefaultAuthSeqMgtException;

    /**
     * Retrieve default authentication sequence basic info.
     *
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    public abstract DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String tenantDomain)
            throws DefaultAuthSeqMgtException;

    /**
     * Check existence of default authentication sequence.
     *
     * @param tenantDomain tenant domain
     * @return true if a default authentication sequence exists for the tenant
     * @throws DefaultAuthSeqMgtException
     */
    public abstract boolean isExistingDefaultAuthenticationSequence(String tenantDomain)
            throws DefaultAuthSeqMgtException;

    /**
     * Delete default authentication sequence.
     *
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtException
     */
    public abstract void deleteDefaultAuthenticationSeq(String tenantDomain)
            throws DefaultAuthSeqMgtException;

    /**
     * Update default authentication sequence.
     *
     * @param sequence default authentication sequence
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtException
     */
    public abstract void updateDefaultAuthenticationSeq(DefaultAuthenticationSequence sequence, String tenantDomain)
            throws DefaultAuthSeqMgtException;
}
