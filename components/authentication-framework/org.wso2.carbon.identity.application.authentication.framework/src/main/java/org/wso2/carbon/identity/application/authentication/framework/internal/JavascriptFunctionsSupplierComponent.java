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

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.HasRoleFunction;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.IsExistsStringFunction;

/**
 * Holds and supplies javascript function contribution to the framework.
 */
@Component(
        name = "identity.application.authentication.framework.extension.js.default.functions.component",
        immediate = true)
public class JavascriptFunctionsSupplierComponent {

    private JsFunctionRegistry jsFunctionRegistry;
    private HasRoleFunction hasRoleFunction;

    @Activate
    protected void activate(ComponentContext ctxt) {
        hasRoleFunction = new HasRoleFunction();
        jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "hasRole",
                (IsExistsStringFunction) hasRoleFunction::contains);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (jsFunctionRegistry != null) {
            jsFunctionRegistry.deRegister(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "hasRole");
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
