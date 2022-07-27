/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework;

import org.openjdk.nashorn.api.scripting.JSObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Definition for dynamic Authentication Decision evaluation.
 * Custom functions can be added supporting this contract to the dynamic sequence handler.
 *
 * @see org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler
 *
 */
@FunctionalInterface
public interface AuthenticationDecisionEvaluator extends Serializable {

    /**
     * Selects the authentication decision outcome based on current context.
     * Implementor may return null, in which case the flow will select the default outcome.
     *
     * @param context
     * @return
     */
    Object evaluate(AuthenticationContext context, Function<JSObject, Object> jsConsumer);
}
