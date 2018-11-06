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

package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtServerException;

/**
 * This interface access the data storage layer to store/update and delete tenant default authentication sequences.
 */
public interface DefaultAuthSeqMgtDAO {

    /**
     * Create default authentication sequence.
     *
     * @param authenticationSequence default authentication sequence
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtServerException
     */
    void createDefaultAuthenticationSeq(DefaultAuthenticationSequence authenticationSequence, String tenantDomain)
            throws DefaultAuthSeqMgtServerException;

    /**
     * Check existence of default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    boolean isDefaultAuthSeqExists(String sequenceName, String tenantDomain) throws DefaultAuthSeqMgtServerException;

    /**
     * Retrieve default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    DefaultAuthenticationSequence getDefaultAuthenticationSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtServerException;

    /**
     * Retrieve default authentication sequence basic info.
     *
     * @param sequenceName name of the default authentication sequence
     * @param tenantDomain tenant domain
     * @return default authentication sequence
     * @throws DefaultAuthSeqMgtServerException
     */
    DefaultAuthenticationSequence getDefaultAuthenticationSeqInfo(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtServerException;

    /**
     * Update default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @param sequence default authentication sequence
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtServerException
     */
    void updateDefaultAuthenticationSeq(String sequenceName, DefaultAuthenticationSequence sequence,
                                        String tenantDomain) throws DefaultAuthSeqMgtServerException;

    /**
     * Delete default authentication sequence.
     *
     * @param sequenceName name of the default authentication sequence
     * @param tenantDomain tenant domain
     * @throws DefaultAuthSeqMgtServerException
     */
    void deleteDefaultAuthenticationSeq(String sequenceName, String tenantDomain)
            throws DefaultAuthSeqMgtServerException;
}
