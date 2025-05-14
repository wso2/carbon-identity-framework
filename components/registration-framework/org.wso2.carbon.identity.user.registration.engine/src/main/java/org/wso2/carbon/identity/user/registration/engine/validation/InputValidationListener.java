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

package org.wso2.carbon.identity.user.registration.engine.validation;

import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;

/**
 * Listener to handle input validation.
 */
public class InputValidationListener extends AbstractFlowExecutionListener {

    @Override
    public int getDefaultOrderId() {

        return 1;
    }

    @Override
    public int getExecutionOrderId() {

        return 2;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public boolean doPostInitiate(RegistrationStep step, RegistrationContext registrationContext)
            throws RegistrationEngineException {

        InputValidationService.getInstance().handleStepInputs(step.getData(), registrationContext);
        return true;
    }

    @Override
    public boolean doPreContinue(RegistrationContext registrationContext)
            throws RegistrationEngineException {

        InputValidationService.getInstance().validateInputs(registrationContext);
        InputValidationService.getInstance().handleUserInputs(registrationContext);
        return true;
    }

    @Override
    public boolean doPostContinue(RegistrationStep step, RegistrationContext registrationContext)
            throws RegistrationEngineException {

        InputValidationService.getInstance().handleStepInputs(step.getData(), registrationContext);
        InputValidationService.getInstance().clearUserInputs(registrationContext);
        return true;
    }
}
