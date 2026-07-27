/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.consent.mgt.core.listener.AbstractConsentManagementListener;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

/**
 * Cleans up consent purpose-application mappings when a purpose is deleted.
 */
public class ConsentAppMappingPurposeDeleteListener extends AbstractConsentManagementListener {

    private static final Log LOG = LogFactory.getLog(ConsentAppMappingPurposeDeleteListener.class);

    @Override
    public int getDefaultOrderId() {

        return 100;
    }

    @Override
    public void postDeletePurpose(String purposeUuid, String tenantDomain) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing consent application mappings for deleted purpose: " + purposeUuid);
        }
        try {
            FrameworkServiceDataHolder.getInstance().getConsentAppMappingService()
                    .removeAllApplicationMappingsForPurpose(purposeUuid);
        } catch (ConsentAppMappingException e) {
            LOG.warn("Failed to remove application mappings for deleted consent purpose: " + purposeUuid, e);
        }
    }
}
