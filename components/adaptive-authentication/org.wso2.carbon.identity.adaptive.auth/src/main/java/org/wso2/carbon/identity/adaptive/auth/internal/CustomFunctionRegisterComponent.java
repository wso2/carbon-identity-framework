/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.adaptive.auth.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.adaptive.auth.Constants;
import org.wso2.carbon.identity.adaptive.auth.js.PublishEventFunctionImpl;
import org.wso2.carbon.identity.adaptive.auth.js.QuerySiddhiRuntimeFunctionImpl;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;

@Component(
        name = "siddhi.custom.functions.component",
        immediate = true)
public class CustomFunctionRegisterComponent {

    private JsFunctionRegistry jsFunctionRegistry;

    @Activate
    protected void activate(ComponentContext ctxt) {

        jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, Constants.FUNC_PUBLISH_EVENT, new
                PublishEventFunctionImpl());
        jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, Constants
                .FUNC_QUERY_SIDDHI_RUNTIME, new QuerySiddhiRuntimeFunctionImpl());
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (jsFunctionRegistry != null) {
            jsFunctionRegistry.deRegister(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, Constants.FUNC_PUBLISH_EVENT);
            jsFunctionRegistry.deRegister(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, Constants
                    .FUNC_QUERY_SIDDHI_RUNTIME);
        }
    }

    @Reference(
            service = JsFunctionRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJsFunctionRegistry"
    )
    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }

    public void unsetJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = null;
    }
}
