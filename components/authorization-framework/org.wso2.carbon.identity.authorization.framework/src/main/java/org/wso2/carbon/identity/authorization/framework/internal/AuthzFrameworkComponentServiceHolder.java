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
import org.wso2.carbon.identity.authorization.framework.service.AccessEvaluationService;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for the Authorization Framework component.
 */
public class AuthzFrameworkComponentServiceHolder {

    private static final Log LOG = LogFactory.getLog(AuthzFrameworkComponentServiceHolder.class);
    private static AuthzFrameworkComponentServiceHolder instance = new AuthzFrameworkComponentServiceHolder();
    private Map<String, AccessEvaluationService> accessEvaluationServices;

    private AuthzFrameworkComponentServiceHolder() {

        this.accessEvaluationServices = new HashMap<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("AuthzFrameworkComponentServiceHolder initialized successfully.");
        }
    }

    public static AuthzFrameworkComponentServiceHolder getInstance() {

        return instance;
    }

    public void addAccessEvaluationService(AccessEvaluationService accessEvaluationService) {

        if (accessEvaluationService != null) {
            String engineName;
            if (!StringUtils.isBlank(accessEvaluationService.getEngine())) {
                engineName = accessEvaluationService.getEngine();
                this.accessEvaluationServices.put(engineName, accessEvaluationService);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Access evaluation service added successfully for engine: " + engineName);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authorization engine name is not defined in the service. Hence, the service is not " +
                            "added.");
                }
            }
        } else {
            LOG.warn("Null access evaluation service received, hence not adding service.");
        }
    }

    public Map<String, AccessEvaluationService> getAllAccessEvaluationServices() {

        return this.accessEvaluationServices;
    }

    public AccessEvaluationService getAccessEvaluationService(String engineName) {

        if (StringUtils.isBlank(engineName)) {
            LOG.warn("Engine name is null or empty. Cannot retrieve access evaluation service.");
            return null;
        }
        AccessEvaluationService service = this.accessEvaluationServices.get(engineName);
        if (service == null && LOG.isDebugEnabled()) {
            LOG.debug("No access evaluation service found for engine: " + engineName);
        }
        return service;
    }

    public void removeAccessEvaluationService(AccessEvaluationService accessEvaluationService) {

        if (accessEvaluationService != null) {
            String engineName = accessEvaluationService.getEngine();
            if (engineName != null) {
                this.accessEvaluationServices.remove(engineName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Access evaluation service removed for engine: " + engineName);
                }
            } else {
                LOG.warn("Engine name is null, cannot remove access evaluation service.");
            }
        } else {
            LOG.warn("Null access evaluation service received, hence cannot remove service.");
        }
    }
}
