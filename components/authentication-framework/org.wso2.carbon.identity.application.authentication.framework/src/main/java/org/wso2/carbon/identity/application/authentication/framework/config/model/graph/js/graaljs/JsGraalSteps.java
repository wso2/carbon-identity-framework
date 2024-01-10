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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSContextMemberObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Optional;

/**
 * Returns when context.steps[<step_number] is called
 * This wrapper uses GraalJS polyglot context.
 */
public class JsGraalSteps extends AbstractJSContextMemberObject implements ProxyArray {

    private static final Log LOG = LogFactory.getLog(JsGraalSteps.class);

    public JsGraalSteps() {

    }

    public JsGraalSteps(AuthenticationContext context) {

        initializeContext(context);
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

    @Override
    public Object get(long index) {

        if (getContext() == null) {
            return null;
        } else {
            return new JsGraalStep(getContext(), (int) index, getAuthenticatedIdPOfStep((int) index));
        }
    }

    @Override
    public void set(long index, Value value) {
        //Steps can not be set with script.
    }

    @Override
    public long getSize() {

        if (getContext() == null) {
            return 0;
        } else {
            return getContext().getSequenceConfig().getStepMap().size();
        }
    }

}
