/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.List;
import java.util.Map;

/**
 * This interface access the data storage layer to store/update and delete application configurations.
 */
public interface ApplicationDAO {

    /**
     * @param applicationDTO
     * @return
     * @throws IdentityApplicationManagementException
     */
    int createApplication(ServiceProvider applicationDTO, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * @param applicationName
     * @return
     * @throws IdentityApplicationManagementException
     */
    ServiceProvider getApplication(String applicationName, String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * @param applicationId
     * @return
     * @throws IdentityApplicationManagementException
     */
    ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException;

    /**
     * @return
     * @throws IdentityApplicationManagementException
     */
    ApplicationBasicInfo[] getAllApplicationBasicInfo() throws IdentityApplicationManagementException;

    /**
     * @param applicationDTO
     * @throws IdentityApplicationManagementException
     */
    void updateApplication(ServiceProvider applicationDTO, String tenantDomain) throws
            IdentityApplicationManagementException;

    /**
     * @param applicationName
     * @throws IdentityApplicationManagementException
     */
    void deleteApplication(String applicationName) throws IdentityApplicationManagementException;

    /**
     * @param applicationID
     * @return
     * @throws IdentityApplicationManagementException
     */
    String getApplicationName(int applicationID) throws IdentityApplicationManagementException;

    /**
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    String getServiceProviderNameByClientId(String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * [sp-claim-uri,local-idp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     */
    Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                 String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * [local-idp-claim-uri,sp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                 String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Returns back the requested set of claims by the provided service provider in local idp claim
     * dialect.
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     */
    List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                        String tenantDomain) throws IdentityApplicationManagementException;
}
