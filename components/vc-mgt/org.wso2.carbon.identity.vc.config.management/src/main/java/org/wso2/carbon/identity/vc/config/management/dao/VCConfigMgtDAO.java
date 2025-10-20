/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.vc.config.management.dao;

import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.List;

/**
 * DAO for VC Config persistence.
 */
public interface VCConfigMgtDAO {

    List<VCCredentialConfiguration> list(int tenantId) throws VCConfigMgtException;

    VCCredentialConfiguration getByConfigId(String configId, int tenantId) throws VCConfigMgtException;

    boolean existsByIdentifier(String identifier, int tenantId) throws VCConfigMgtException;

    boolean existsByConfigurationId(String configurationId, int tenantId) throws VCConfigMgtException;

    VCCredentialConfiguration create(VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException;

    VCCredentialConfiguration update(String configId, VCCredentialConfiguration configuration, int tenantId)
            throws VCConfigMgtException;

    void delete(String configId, int tenantId) throws VCConfigMgtException;
}
