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


package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FileBasedApplicationDAO implements ApplicationDAO {

    @Override
    public int createApplication(ServiceProvider applicationDTO, String tenantDomain)
            throws IdentityApplicationManagementException {
        throw new IdentityApplicationManagementException("Not supported in file based dao.");
    }

    @Override
    public ServiceProvider getApplication(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException {
        return ApplicationManagementServiceComponent.getFileBasedSPs().get(applicationName);
    }

    @Override
    public ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException {
        throw new IdentityApplicationManagementException("Not supported in file based dao.");
    }

    @Override
    public ApplicationBasicInfo[] getAllApplicationBasicInfo()
            throws IdentityApplicationManagementException {
        Map<String, ServiceProvider> spMap = ApplicationManagementServiceComponent
                .getFileBasedSPs();

        List<ApplicationBasicInfo> appInfo = new ArrayList<ApplicationBasicInfo>();

        for (Iterator<Entry<String, ServiceProvider>> iterator = spMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, ServiceProvider> entry = iterator.next();
            ApplicationBasicInfo basicInfo = new ApplicationBasicInfo();
            basicInfo.setApplicationName(entry.getValue().getApplicationName());
            basicInfo.setDescription(entry.getValue().getDescription());
            appInfo.add(basicInfo);

        }

        return appInfo.toArray(new ApplicationBasicInfo[appInfo.size()]);
    }

    @Override
    public void updateApplication(ServiceProvider applicationDTO, String tenantDomain)
            throws IdentityApplicationManagementException {
        throw new IdentityApplicationManagementException("Not supported in file based dao.");
    }

    @Override
    public void deleteApplication(String applicationName)
            throws IdentityApplicationManagementException {
        throw new IdentityApplicationManagementException("Not supported in file based dao.");
    }

    @Override
    public String getApplicationName(int applicationID)
            throws IdentityApplicationManagementException {

        Map<String, ServiceProvider> spMap = ApplicationManagementServiceComponent
                .getFileBasedSPs();

        for (Iterator<Entry<String, ServiceProvider>> iterator = spMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, ServiceProvider> entry = iterator.next();

            if (entry.getValue().getApplicationID() == applicationID) {
                return entry.getValue().getApplicationName();
            }

        }

        return null;
    }

    @Override
    public String getServiceProviderNameByClientId(String clientId, String clientType,
                                                   String tenantDomain) throws IdentityApplicationManagementException {

        Map<String, ServiceProvider> spMap = ApplicationManagementServiceComponent
                .getFileBasedSPs();

        for (Iterator<Entry<String, ServiceProvider>> iterator = spMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, ServiceProvider> entry = iterator.next();
            if (entry.getValue().getInboundAuthenticationConfig() != null) {
                InboundAuthenticationRequestConfig[] authRequestConfigs = entry.getValue()
                        .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs();

                if (authRequestConfigs != null && authRequestConfigs.length > 0) {
                    for (InboundAuthenticationRequestConfig config : authRequestConfigs) {
                        if (clientType.equals(config.getInboundAuthType())
                                && clientId.equals(config.getInboundAuthKey())) {
                            return entry.getKey();
                        }
                    }
                }
            }

        }

        return null;
    }

    @Override
    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                        String tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs()
                .get(serviceProviderName);
        Map<String, String> claimMap = new HashMap<String, String>();

        if (serviceProvider == null || serviceProvider.getClaimConfig() == null) {
            return claimMap;
        }

        ClaimMapping[] claimMappings = serviceProvider.getClaimConfig().getClaimMappings();

        if (claimMappings != null && claimMappings.length > 0) {

            for (ClaimMapping mapping : claimMappings) {
                if (mapping.getLocalClaim() != null
                        && mapping.getLocalClaim().getClaimUri() != null
                        && mapping.getRemoteClaim() != null
                        && mapping.getRemoteClaim().getClaimUri() != null) {
                    claimMap.put(mapping.getRemoteClaim().getClaimUri(), mapping.getLocalClaim()
                            .getClaimUri());
                }
            }
        }

        return claimMap;

    }

    @Override
    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                        String tenantDomain) throws IdentityApplicationManagementException {
        ServiceProvider serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs()
                .get(serviceProviderName);
        Map<String, String> claimMap = new HashMap<String, String>();

        if (serviceProvider == null || serviceProvider.getClaimConfig() == null) {
            return claimMap;
        }

        ClaimMapping[] claimMappings = serviceProvider.getClaimConfig().getClaimMappings();

        if (claimMappings != null && claimMappings.length > 0) {

            for (ClaimMapping mapping : claimMappings) {
                if (mapping.getLocalClaim() != null
                        && mapping.getLocalClaim().getClaimUri() != null
                        && mapping.getRemoteClaim() != null
                        && mapping.getRemoteClaim().getClaimUri() != null) {
                    claimMap.put(mapping.getLocalClaim().getClaimUri(), mapping.getRemoteClaim()
                            .getClaimUri());
                }
            }
        }

        return claimMap;
    }

    @Override
    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                               String tenantDomain) throws IdentityApplicationManagementException {
        ServiceProvider serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs()
                .get(serviceProviderName);

        List<String> requestedClaimList = new ArrayList<String>();

        if (serviceProvider == null || serviceProvider.getClaimConfig() == null) {
            return requestedClaimList;
        }

        ClaimMapping[] claimMappings = serviceProvider.getClaimConfig().getClaimMappings();

        if (claimMappings != null && claimMappings.length > 0) {

            for (ClaimMapping mapping : claimMappings) {
                if (mapping.isRequested()) {
                    if (mapping.getRemoteClaim() != null
                            && mapping.getRemoteClaim().getClaimUri() != null) {
                        requestedClaimList.add(mapping.getRemoteClaim().getClaimUri());
                    } else if (mapping.getLocalClaim() != null
                            && mapping.getLocalClaim().getClaimUri() != null) {
                        requestedClaimList.add(mapping.getLocalClaim().getClaimUri());
                    }
                }
            }
        }

        return requestedClaimList;
    }

    @Override
    public boolean isApplicationExists(String serviceProviderName, String tenantName) {

        return ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(serviceProviderName);
    }

}
