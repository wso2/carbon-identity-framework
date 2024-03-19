/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.CommonJsHeaders;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class JsHeaders extends CommonJsHeaders implements AbstractJsObject {

    public JsHeaders(Map wrapped, HttpServletResponse response) {

        super(wrapped, response);
    }

    @Override
    public Object getMember(String name) {

        Object member = super.getMember(name);
        return member != null ? member : AbstractJsObject.super.getMember(name);
    }

    @Override
    public void removeMember(String name) {

        boolean isRemoved = super.removeMemberObject(name);
        if (!isRemoved) {
            AbstractJsObject.super.removeMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        boolean isSet = super.setMemberObject(name, value);
        if (!isSet) {
            AbstractJsObject.super.setMember(name, value);
        }
    }
}
