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

package org.wso2.carbon.identity.flow.execution.engine.rule;

import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;

/**
 * Supplies condition values for the registration flow.
 * <p>
 * Providers are registered one per flow type, so each flow type needs its own instance even where
 * the behaviour is shared. Keeping that behaviour in the base class is what lets these diverge
 * later -- a registration flow has no persisted user until onboarding, for instance -- without the
 * divergence arriving by accident.
 */
public class RegistrationFlowRuleEvaluationDataProvider extends AbstractFlowRuleEvaluationDataProvider {

    @Override
    public FlowType getSupportedFlowType() {

        return FlowType.REGISTRATION;
    }
}
