/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.captcha.mgt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.captcha.mgt.util.CaptchaUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "org.wso2.carbon.captcha.mgt.internal.CaptchaMgtServiceComponent",
        immediate = true
)
public class CaptchaMgtServiceComponent {

    private static Log log = LogFactory.getLog(CaptchaMgtServiceComponent.class);
    private static ConfigurationContextService configurationContextService;
    private static RegistryService registryService;

    public static ConfigurationContext getConfigurationContext() {

        if (configurationContextService.getServerConfigContext() == null) {
            return null;
        }
        return configurationContextService.getServerConfigContext();
    }

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {

        return registryService.getConfigSystemRegistry(tenantId);
    }

    @Activate
    protected void activate(ComponentContext context) {

        try {
            CaptchaUtil.setAnonAccessToCaptchaImages();
            if (log.isDebugEnabled()) {
                log.debug("*************Captcha Management component is activated.**************");
            }
        } catch (Exception e) {
            log.error("******* Captcha Management Component failed activating ****", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("******* Captcha Management bundle is deactivated ******* ");
    }

    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {

        log.debug("Receiving ConfigurationContext Service");
        this.configurationContextService = configurationContextService;

    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

        log.debug("Unsetting ConfigurationContext Service");
        this.configurationContextService = null;
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        this.registryService = null;
    }

}
