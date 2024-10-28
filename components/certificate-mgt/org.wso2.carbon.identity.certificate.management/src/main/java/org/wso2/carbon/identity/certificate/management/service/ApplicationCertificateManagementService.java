/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.certificate.management.service;

import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * Interface for managing trusted certificates of applications of a tenant.
 * This service supports using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CertificateManagementService}, which supports operations with UUID.
 */
@Deprecated
public interface ApplicationCertificateManagementService {

    /**
     * Add a certificate.
     *
     * @param certificate  Certificate information.
     * @param tenantDomain Tenant domain.
     * @return Auto incremented integer id of the Certificate.
     * @throws CertificateMgtException If an error occurs while adding the certificate.
     */
    @Deprecated
    int addCertificate(Certificate certificate, String tenantDomain) throws CertificateMgtException;

    /**
     * Get certificate information with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate.
     */
    @Deprecated
    Certificate getCertificate(int certificateId, String tenantDomain) throws CertificateMgtException;

    /**
     * Get certificate information with given name.
     *
     * @param certificateName Certificate name.
     * @param tenantDomain    Tenant domain.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate by name.
     */
    @Deprecated
    Certificate getCertificateByName(String certificateName, String tenantDomain) throws CertificateMgtException;

    /**
     * Update a certificate with given id.
     *
     * @param certificateId      Certificate ID.
     * @param certificateContent Certificate content.
     * @param tenantDomain       Tenant domain.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Deprecated
    void updateCertificateContent(int certificateId, String certificateContent, String tenantDomain)
            throws CertificateMgtException;

    /**
     * Delete a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantDomain  Tenant domain.
     * @throws CertificateMgtException If an error occurs while deleting the certificate.
     */
    @Deprecated
    void deleteCertificate(int certificateId, String tenantDomain) throws CertificateMgtException;
}
