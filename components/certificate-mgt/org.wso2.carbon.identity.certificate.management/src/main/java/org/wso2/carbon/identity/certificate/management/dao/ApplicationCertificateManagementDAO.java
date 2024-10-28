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

package org.wso2.carbon.identity.certificate.management.dao;

import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

/**
 * This interface defines the Application Certificate Management DAO.
 * This supports using auto-incremented IDs for certificate management operations in application-mgt.
 *
 * @deprecated It is recommended to use {@link CertificateManagementDAO}, which supports operations with UUID.
 */
@Deprecated
public interface ApplicationCertificateManagementDAO {

    /**
     * Add a certificate.
     *
     * @param certificateId Certificate ID.
     * @param certificate   Certificate information.
     * @param tenantId      Tenant Id.
     * @return Auto incremented integer id of the Certificate.
     * @throws CertificateMgtException If an error occurs while adding the certificate.
     */
    @Deprecated
    int addCertificate(String certificateId, Certificate certificate, int tenantId)
            throws CertificateMgtException;

    /**
     * Get certificate information with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantId      Tenant Id.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate.
     */
    @Deprecated
    Certificate getCertificate(int certificateId, int tenantId) throws CertificateMgtException;

    /**
     * Get certificate information with given name.
     *
     * @param certificateName Certificate name.
     * @param tenantId        Tenant Id.
     * @return Certificate information.
     * @throws CertificateMgtException If an error occurs while getting the certificate by name.
     */
    @Deprecated
    Certificate getCertificateByName(String certificateName, int tenantId) throws CertificateMgtException;

    /**
     * Update a certificate with given id.
     *
     * @param certificateId      Certificate ID.
     * @param certificateContent Certificate content.
     * @param tenantId           Tenant Id.
     * @throws CertificateMgtException If an error occurs while updating the certificate.
     */
    @Deprecated
    void updateCertificateContent(int certificateId, String certificateContent, int tenantId)
            throws CertificateMgtException;

    /**
     * Delete a certificate with given id.
     *
     * @param certificateId Certificate ID.
     * @param tenantId      Tenant Id.
     * @throws CertificateMgtException If an error occurs while deleting the certificate.
     */
    @Deprecated
    void deleteCertificate(int certificateId, int tenantId) throws CertificateMgtException;
}
