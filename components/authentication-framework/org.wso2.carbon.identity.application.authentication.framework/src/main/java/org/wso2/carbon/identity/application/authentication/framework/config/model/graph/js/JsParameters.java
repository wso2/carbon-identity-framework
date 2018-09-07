/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers/cookies.
 * This provides controlled access to HTTPServletRequest object's headers and cookies via provided javascript native
 * syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class JsParameters extends AbstractJSObjectWrapper<Map> {

    private static final Log LOG = LogFactory.getLog(JsParameters.class);

    public JsParameters(Map wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {
        return getWrapped().get(name);
    }

    @Override
    public boolean hasMember(String name) {
        return getWrapped().get(name) != null;
    }

    @Override
    public void removeMember(String name) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't remove parameter " + name);
    }

    @Override
    public void setMember(String name, Object value) {

        LOG.warn("Unsupported operation. Parameters are read only. Can't set parameter " + name + " to value: " + value);
    }
}
