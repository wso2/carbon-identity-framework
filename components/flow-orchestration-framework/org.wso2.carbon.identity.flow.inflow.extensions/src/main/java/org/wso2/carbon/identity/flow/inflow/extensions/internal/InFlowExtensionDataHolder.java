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

package org.wso2.carbon.identity.flow.inflow.extensions.internal;

import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.flow.inflow.extensions.config.FlowContextHandoverConfig;

/**
 * Data holder for the In-Flow Extension bundle.
 */
public class InFlowExtensionDataHolder {

    private static final InFlowExtensionDataHolder instance = new InFlowExtensionDataHolder();

    private ActionExecutorService actionExecutorService;
    private ActionManagementService actionManagementService;
    private CertificateManagementService certificateManagementService;
    private FlowContextHandoverConfig flowContextHandoverConfig;

    private InFlowExtensionDataHolder() {

    }

    public static InFlowExtensionDataHolder getInstance() {

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

    /**
     * Get the In-Flow Extension context handover config. Lazily initialised on first access
     * (the constructor reads from {@code IdentityUtil} which requires the carbon configuration
     * to be loaded — keeping this lazy avoids ordering issues with OSGi component activation).
     *
     * @return The handover config, never null.
     */
    public synchronized FlowContextHandoverConfig getFlowContextHandoverConfig() {

        if (flowContextHandoverConfig == null) {
            flowContextHandoverConfig = new FlowContextHandoverConfig();
        }
        return flowContextHandoverConfig;
    }

    /**
     * Override the handover config. Intended for tests only.
     */
    public synchronized void setFlowContextHandoverConfig(FlowContextHandoverConfig flowContextHandoverConfig) {

        this.flowContextHandoverConfig = flowContextHandoverConfig;
    }
}
