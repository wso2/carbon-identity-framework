/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.authorization.framework.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.authorization.framework.service.AccessEvaluationService;

/**
 * OSGi declarative services component which handles registration and deregistration of Authorization Framework
 * component.
 */
@Component(
        name = "identity.application.authorization.framework.component",
        immediate = true
)
public class AuthzFrameworkServiceComponent {

    private static final Log LOG = LogFactory.getLog(AuthzFrameworkServiceComponent.class);

    @Reference(
            name = "org.wso2.carbon.identity.authorization.framework.service.AccessEvaluationService",
            service = AccessEvaluationService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAccessEvaluationInterface"
    )
    protected void setAccessEvaluationInterface(AccessEvaluationService accessEvaluationService) {

        if (accessEvaluationService == null) {
            LOG.warn("Null Access Evaluation service received, hence not registering");
            return;
        }
        if (!StringUtils.isBlank(accessEvaluationService.getEngine())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting Access Evaluation service :" + accessEvaluationService.getClass().getName());
            }
            AuthzFrameworkComponentServiceHolder.getInstance().addAccessEvaluationService(accessEvaluationService);
        } else {
            LOG.warn("Engine name is not defined for the Access Evaluation service: " + accessEvaluationService
                    .getClass().getName() + ". Hence not registering.");
        }
    }

    protected void unsetAccessEvaluationInterface(AccessEvaluationService accessEvaluationService) {

        if (accessEvaluationService == null) {
            LOG.warn("Null Access Evaluation service received, hence cannot unbind.");
            return;
        }
        if (AuthzFrameworkComponentServiceHolder.getInstance().getAllAccessEvaluationServices()
                .containsKey(accessEvaluationService.getEngine())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("unbinding Access Evaluation service :" + accessEvaluationService.getClass().getName());
            }
            AuthzFrameworkComponentServiceHolder.getInstance().removeAccessEvaluationService(accessEvaluationService);
        } else {
            LOG.warn("No registered service found for the Access Evaluation service: " + accessEvaluationService
                    .getClass().getName());
        }
    }

}
