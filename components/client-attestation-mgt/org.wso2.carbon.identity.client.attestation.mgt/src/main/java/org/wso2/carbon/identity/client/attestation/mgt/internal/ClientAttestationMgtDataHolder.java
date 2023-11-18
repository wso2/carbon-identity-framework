/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.internal;


import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import java.security.cert.X509Certificate;

/**
 * The `ClientAttestationMgtDataHolder` class serves as a data holder for managing
 * client attestation-related data and services.
 */
public class ClientAttestationMgtDataHolder {

    private ApplicationManagementService applicationManagementService;

    private static ClientAttestationMgtDataHolder instance
            = new ClientAttestationMgtDataHolder();

    private X509Certificate appleAttestationRootCertificate;
    private boolean appleAttestationRevocationCheckEnabled;

    private ClientAttestationMgtDataHolder() {

    }

    public static ClientAttestationMgtDataHolder getInstance() {

        return instance;
    }

    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    public X509Certificate getAppleAttestationRootCertificate() {

        return appleAttestationRootCertificate;
    }

    public void setAppleAttestationRootCertificate(X509Certificate appleAttestationRootCertificate) {

        this.appleAttestationRootCertificate = appleAttestationRootCertificate;
    }

    public boolean isAppleAttestationRevocationCheckEnabled() {

        return appleAttestationRevocationCheckEnabled;
    }

    public void setAppleAttestationRevocationCheckEnabled(boolean appleAttestationRevocationCheckEnabled) {

        this.appleAttestationRevocationCheckEnabled = appleAttestationRevocationCheckEnabled;
    }
}
