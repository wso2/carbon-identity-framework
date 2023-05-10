/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store the optimized step config attributes in session context.
 * It takes the large step config object and optimizes it into a more compact and efficient format.
 */
public class OptimizedStepConfig implements Serializable {

    private static final long serialVersionUID = -3891975634264763833L;

    private final int order;
    private final String loginPage;
    private final AuthenticatedUser authenticatedUser;
    private final boolean subjectIdentifierStep;
    private final boolean subjectAttributeStep;
    private final String authenticatedIdP;
    private final String authenticatedAuthenticatorName;
    private final List<OptimizedAuthenticatorConfig> optimizedAuthenticatorList;
    private final List<String> authenticatorMappings;
    private final boolean completed;
    private final boolean multiOption;
    private final boolean retrying;
    private final boolean forced;

    private static final Log LOG = LogFactory.getLog(OptimizedStepConfig.class);

    public OptimizedStepConfig(StepConfig stepConfig) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimization process for the step config has started.");
        }
        this.order = stepConfig.getOrder();
        this.loginPage = stepConfig.getLoginPage();
        this.authenticatedUser = stepConfig.getAuthenticatedUser();
        this.subjectIdentifierStep = stepConfig.isSubjectIdentifierStep();
        this.subjectAttributeStep = stepConfig.isSubjectAttributeStep();
        this.authenticatedIdP = stepConfig.getAuthenticatedIdP();
        this.authenticatedAuthenticatorName = stepConfig.getAuthenticatedAutenticator() != null ?
                stepConfig.getAuthenticatedAutenticator().getName() : null;
        this.optimizedAuthenticatorList = getOptimizedAuthenticatorList(stepConfig.getAuthenticatorList());
        this.authenticatorMappings = stepConfig.getAuthenticatorMappings();
        this.completed = stepConfig.isCompleted();
        this.multiOption = stepConfig.isMultiOption();
        this.retrying = stepConfig.isRetrying();
        this.forced = stepConfig.isForced();
    }

    private List<OptimizedAuthenticatorConfig> getOptimizedAuthenticatorList(List<AuthenticatorConfig>
                                                                                     authenticatorList) {

        List<OptimizedAuthenticatorConfig> optimizedAuthenticatorList = new ArrayList<>();
        for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
            optimizedAuthenticatorList.add(new OptimizedAuthenticatorConfig(authenticatorConfig));
        }
        return optimizedAuthenticatorList;
    }

    /**
     * This method is used to get the step config.
     *
     * @return Step config.
     */
    public StepConfig getStepConfig() throws SessionDataStorageOptimizationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading process for the step config has started.");
        }
        StepConfig stepConfig = new StepConfig();
        stepConfig.setOrder(this.order);
        stepConfig.setLoginPage(this.loginPage);
        stepConfig.setAuthenticatedUser(this.authenticatedUser);
        stepConfig.setSubjectIdentifierStep(this.subjectIdentifierStep);
        stepConfig.setSubjectAttributeStep(this.subjectAttributeStep);
        stepConfig.setAuthenticatedIdP(this.authenticatedIdP);
        List<AuthenticatorConfig> authenticatorList = new ArrayList<>();
        for (OptimizedAuthenticatorConfig optimizedAuthenticatorConfig : this.optimizedAuthenticatorList) {
            AuthenticatorConfig authConfig = optimizedAuthenticatorConfig.getAuthenticatorConfig();
            authenticatorList.add(authConfig);
            if (authConfig.getName().equals(this.authenticatedAuthenticatorName)) {
                stepConfig.setAuthenticatedAutenticator(authConfig);
            }
        }
        stepConfig.setAuthenticatorList(authenticatorList);
        stepConfig.setAuthenticatorMappings(this.authenticatorMappings);
        stepConfig.setCompleted(this.completed);
        stepConfig.setMultiOption(this.multiOption);
        stepConfig.setRetrying(this.retrying);
        stepConfig.setForced(this.forced);
        return stepConfig;
    }
}
