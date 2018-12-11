/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.template.mgt.TemplateManager;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "carbon.identity.template.mgt.ui",
        immediate = true
)
public class TemplateMgtUIServiceComponent {

    private static final Log log = LogFactory.getLog(TemplateMgtUIServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Template Management UI bundle activated!");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Template Management UI bundle is deactivated");
        }
    }

    @Reference(
            name = "carbon.identity.template.mgt.component",
            service = org.wso2.carbon.identity.template.mgt.TemplateManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTemplateManagementService"
    )
    protected void setTemplateManagementService(TemplateManager templateManagementService) {

        TemplateManagementUIServiceDataHolder.getInstance().setTemplateManager(templateManagementService);
        if (templateManagementService != null && log.isDebugEnabled()) {
            log.debug("TemplateManager is registered in TemplateManager UI service.");
        }
    }

    protected void unsetTemplateManagementService(TemplateManager templateManagementService) {

        TemplateManagementUIServiceDataHolder.getInstance().setTemplateManager(null);
        if (log.isDebugEnabled()) {
            log.debug("TemplateManager is unregistered in TemplateManager UI service.");
        }
    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        TemplateManagementUIServiceDataHolder.getInstance().setRealmService(realmService);
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("RealmService is registered in ConsentManager service.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        TemplateManagementUIServiceDataHolder.getInstance().setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unregistered in ConsentManager service.");
        }
    }
}
