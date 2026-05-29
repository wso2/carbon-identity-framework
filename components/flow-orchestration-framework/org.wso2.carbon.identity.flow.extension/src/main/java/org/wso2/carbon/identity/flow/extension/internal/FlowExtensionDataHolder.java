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

package org.wso2.carbon.identity.flow.extension.internal;

import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;

/**
 * Data holder for the In-Flow Extension bundle.
 *
 * <p>This singleton is used by DS bind/unbind methods to expose dynamic OSGi references
 * to classes outside the component lifecycle methods.</p>
 */
public final class FlowExtensionDataHolder {

    private static final FlowExtensionDataHolder instance = new FlowExtensionDataHolder();

    private ActionExecutorService actionExecutorService;
    private ActionManagementService actionManagementService;
    private CertificateManagementService certificateManagementService;
    private ClaimMetadataManagementService claimMetadataManagementService;

    private FlowExtensionDataHolder() {

    }

    public static FlowExtensionDataHolder getInstance() {

        return instance;
    }

    public ActionExecutorService getActionExecutorService() {

        return actionExecutorService;
    }

    public void setActionExecutorService(ActionExecutorService actionExecutorService) {

        this.actionExecutorService = actionExecutorService;
    }

    public ActionManagementService getActionManagementService() {

        return actionManagementService;
    }

    public void setActionManagementService(ActionManagementService actionManagementService) {

        this.actionManagementService = actionManagementService;
    }

    public CertificateManagementService getCertificateManagementService() {

        return certificateManagementService;
    }

    public void setCertificateManagementService(CertificateManagementService certificateManagementService) {

        this.certificateManagementService = certificateManagementService;
    }

    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

}
