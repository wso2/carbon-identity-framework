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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperFactoryProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseSteps;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Optional;

/**
 * Returns when context.steps[step_number] is called.
 * This is the abstract wrapper used for all script engine implementations.
 */
public abstract class JsSteps extends AbstractJSContextMemberObject implements JsBaseSteps {

    public JsSteps() {

    }

    public JsSteps(AuthenticationContext context) {

        initializeContext(context);
    }

    public boolean hasSlot(int step) {

        if (getContext() == null) {
            return false;
        } else {
            return getContext().getSequenceConfig().getStepMap().containsKey(step);
        }
    }

    public Object getSlot(int step) {

        if (getContext() == null) {
            return null;
        } else {
            return JsWrapperFactoryProvider.getInstance().getWrapperFactory()
                    .createJsStep(getContext(), step, getAuthenticatedIdPOfStep(step),
                            getAuthenticatedAuthenticatorOfStep(step));
        }
    }

    public Object get(long index) {

        return getSlot((int) index);
    }

    public long getSize() {

        if (getContext() == null) {
            return 0;
        }
        return getContext().getSequenceConfig().getStepMap().size();
    }

    private String getAuthenticatedIdPOfStep(int step) {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        Optional<StepConfig> optionalStepConfig = getContext().getSequenceConfig().getStepMap().values().stream()
                .filter(stepConfig -> stepConfig.getOrder() == step).findFirst();
        return optionalStepConfig.map(StepConfig::getAuthenticatedIdP).orElse(null);
    }

    private String getAuthenticatedAuthenticatorOfStep(int step) {

        if (getContext().getSequenceConfig() == null) {
            // Sequence config is not yet initialized.
            return null;
        }

        Optional<StepConfig> optionalStepConfig = getContext().getSequenceConfig().getStepMap().values().stream()
                .filter(stepConfig -> stepConfig.getOrder() == step).findFirst();
        AuthenticatorConfig authenticatorConfig = optionalStepConfig.map(StepConfig::getAuthenticatedAutenticator)
                .orElse(null);
        return authenticatorConfig != null ? authenticatorConfig.getName() : null;
    }
}
