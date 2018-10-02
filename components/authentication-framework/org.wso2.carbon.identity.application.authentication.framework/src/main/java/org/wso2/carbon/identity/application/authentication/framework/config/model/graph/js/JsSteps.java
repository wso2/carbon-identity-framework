/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Returns when context.steps[<step_number] is called
 */
public class JsSteps extends AbstractJSContextMemberObject {

    private static final Log LOG = LogFactory.getLog(JsSteps.class);

    public JsSteps() {

    }

    public JsSteps(AuthenticationContext context) {

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
            return super.getSlot(step);
        } else {
            return new JsStep(getContext(), step, getAuthenticatedIdPOfStep(step));
        }
    }


    private String getAuthenticatedIdPOfStep(int step) {

        if (getContext().getSequenceConfig() == null) {
            //Sequence config is not yet initialized
            return null;
        }

        StepConfig stepConfig = getContext().getSequenceConfig().getAuthenticationGraph().getStepMap().get(step);
        if (stepConfig != null) {
            return stepConfig.getAuthenticatedIdP();
        }
        return null;
    }
}
