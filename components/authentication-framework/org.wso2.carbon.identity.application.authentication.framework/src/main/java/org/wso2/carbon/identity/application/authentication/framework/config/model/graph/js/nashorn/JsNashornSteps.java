/*
 *  Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSContextMemberObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Optional;

/**
 * Returns when context.steps[<step_number] is called
 * This wrapper uses jdk.nashorn engine.
 */
public class JsNashornSteps extends AbstractJSContextMemberObject implements AbstractJsObject {

    private static final Log LOG = LogFactory.getLog(JsNashornSteps.class);

    public JsNashornSteps() {

    }

    public JsNashornSteps(AuthenticationContext context) {

        initializeContext(context);
    }

    @Override
    public boolean hasSlot(int step) {

        if (getContext() == null) {
            return false;
        } else {
            return getContext().getSequenceConfig().getStepMap().containsKey(step);
        }
    }

    @Override
    public Object getSlot(int step) {

        if (getContext() == null) {
            return AbstractJsObject.super.getSlot(step);
        } else {
            return new JsNashornStep(getContext(), step, getAuthenticatedIdPOfStep(step));
        }
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
}
