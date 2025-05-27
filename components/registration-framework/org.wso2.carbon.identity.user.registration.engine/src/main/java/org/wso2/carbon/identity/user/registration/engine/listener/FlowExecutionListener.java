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

package org.wso2.carbon.identity.user.registration.engine.listener;

import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;

/**
 * Interface for registration execution listeners.
 * <p>
 * Registration execution listeners are used to perform actions at different stages of the registration flow.
 */
public interface FlowExecutionListener {

    /**
     * Returns the order ID of the listener.
     *
     * @return Order ID.
     */
    int getExecutionOrderId();

    /**
     * Returns the default order ID of the listener.
     *
     * @return Default order ID.
     */
    int getDefaultOrderId();

    /**
     * Returns whether the listener is enabled or not.
     *
     * @return true if enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Pre-execute method to be called before the registration flow is executed.
     *
     * @param registrationContext Registration context.
     * @return true if the pre-initiate was successful, false otherwise.
     * @throws RegistrationEngineException if an error occurs during pre-initiate.
     */
    boolean doPreExecute(RegistrationContext registrationContext) throws RegistrationEngineException;

    /**
     * Post-execute method to be called after the registration flow is executed.
     *
     * @param step                Registration step.
     * @param registrationContext Registration context.
     * @return true if the post-initiate was successful, false otherwise.
     * @throws RegistrationEngineException if an error occurs during post-initiate.
     */
    boolean doPostExecute(RegistrationStep step, RegistrationContext registrationContext)
            throws RegistrationEngineException;

}
