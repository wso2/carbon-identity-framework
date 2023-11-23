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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationServiceImpl;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.APPLE_ATTESTATION_REVOCATION_CHECK_ENABLED;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.APPLE_ATTESTATION_ROOT_CERTIFICATE_PATH;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.CERTIFICATE_EXPIRY_THRESHOLD;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.MILLI_SECOND_IN_DAY;

/**
 * OSGi declarative services component which handled registration and un-registration of
 * ClientAttestationMgtServiceComponent.
 */

@Component(
        name = "identity.client.attestation.mgt.component",
        immediate = true
)
public class ClientAttestationMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(ClientAttestationMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(ClientAttestationService.class.getName(),
                    new ClientAttestationServiceImpl(), null);
            loadConfigs();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Client Attestation Service Component deployed.");
            }

        } catch (Throwable throwable) {
            LOG.error("Error while activating Input Validation Service Component.", throwable);
        }
    }

    /**
     * Loads configurations for the Client Attestation Service.
     */
    private void loadConfigs() {

        // Set the Apple attestation root certificate and revocation check status
        ClientAttestationMgtDataHolder.getInstance()
                .setAppleAttestationRootCertificate(getAppleAttestationRootCertificate());
        ClientAttestationMgtDataHolder.getInstance()
                .setAppleAttestationRevocationCheckEnabled(loadAppleAttestationRevocationCheckEnabled());
    }

    /**
     * Loads the status of Apple attestation revocation check from the configuration.
     *
     * @return True if revocation check is enabled, false otherwise.
     */
    private boolean loadAppleAttestationRevocationCheckEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(APPLE_ATTESTATION_REVOCATION_CHECK_ENABLED));
    }

    /**
     * Retrieves the Apple attestation root certificate from the configured file path.
     *
     * @return The Apple attestation root certificate, or null if not found.
     */
    private X509Certificate getAppleAttestationRootCertificate() {

        try {
            String appleAttestationRootCertificatePath =
                    IdentityUtil.getProperty(APPLE_ATTESTATION_ROOT_CERTIFICATE_PATH);

            if (StringUtils.isNotBlank(appleAttestationRootCertificatePath)) {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
                FileInputStream fileInputStream = new FileInputStream(appleAttestationRootCertificatePath);
                X509Certificate appleAttestationRootCertificate =
                        (X509Certificate) certificateFactory.generateCertificate(fileInputStream);

                // Warn if the certificate is expiring soon
                if (isCertificateExpiringSoon(appleAttestationRootCertificate)) {
                    LOG.warn("Provided apple attestation root certificate is going to expire soon. " +
                            "Please add the latest certificate.");
                }
                return appleAttestationRootCertificate;
            } else {
                LOG.warn("Apple attestation root certificate path is not configured.");
            }
        } catch (CertificateException | FileNotFoundException e) {
            LOG.warn("Apple attestation root certificate not found.", e);
        }
        return null;
    }

    /**
     * Checks if the given X.509 certificate is expiring within 30 days.
     *
     * @param certificate The X.509 certificate to check.
     * @return True if the certificate is expiring soon, false otherwise.
     */
    private boolean isCertificateExpiringSoon(X509Certificate certificate) {

        Date currentDate = new Date();
        Date expirationDate = certificate.getNotAfter();

        // Calculate the difference in days
        long differenceInDays = (expirationDate.getTime() - currentDate.getTime()) / MILLI_SECOND_IN_DAY;

        // Check if the certificate is expiring within 3 months.
        return differenceInDays <= CERTIFICATE_EXPIRY_THRESHOLD;
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Input Validation service component deactivated.");
        }
    }

    @Reference(
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagement"
    )
    public void setApplicationManagement(ApplicationManagementService applicationManagement) {

        ClientAttestationMgtDataHolder.getInstance().setApplicationManagementService(applicationManagement);
    }

    public void unsetApplicationManagement(ApplicationManagementService applicationManagementService) {

        ClientAttestationMgtDataHolder.getInstance().setApplicationManagementService(null);
    }
}
