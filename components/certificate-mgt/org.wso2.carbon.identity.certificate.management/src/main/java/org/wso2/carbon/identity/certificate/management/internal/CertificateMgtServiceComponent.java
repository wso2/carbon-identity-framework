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

package org.wso2.carbon.identity.certificate.management.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.certificate.management.service.ApplicationCertificateManagementService;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.certificate.management.service.impl.ApplicationCertificateManagementServiceImpl;
import org.wso2.carbon.identity.certificate.management.service.impl.CertificateManagementServiceImpl;

/**
 * Service component for the Certificate management.
 */
@Component(
        name = "certificate.mgt.service.component",
        immediate = true
)
public class CertificateMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(CertificateMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(CertificateManagementService.class.getName(),
                    CertificateManagementServiceImpl.getInstance(), null);
            bundleCtx.registerService(ApplicationCertificateManagementService.class.getName(),
                    ApplicationCertificateManagementServiceImpl.getInstance(), null);
            LOG.debug("Certificate management bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing Certificate management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(CertificateManagementService.class));
            bundleCtx.ungetService(bundleCtx.getServiceReference(ApplicationCertificateManagementService.class));
            LOG.debug("Certificate management bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Certificate management component.", e);
        }
    }
}
