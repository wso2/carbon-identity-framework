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

import java.util.Map;

/**
 * Parameters that can be modified from the authentication script.
 * This is the abstract wrapper used for all script engine implementations.
 */
public abstract class JsWritableParameters extends JsParameters {

    public JsWritableParameters(Map wrapped) {

        super(wrapped);
    }

    public void removeMemberObject(String name) {

        getWrapped().remove(name);
    }

    public void setMember(String name, Object value) {

        getWrapped().put(name, value);
    }
}
