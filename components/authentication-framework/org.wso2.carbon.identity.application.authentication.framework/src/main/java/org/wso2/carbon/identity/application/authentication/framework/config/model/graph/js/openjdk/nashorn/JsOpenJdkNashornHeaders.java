/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.CommonJsHeaders;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Javascript wrapper for Java level HashMap of HTTP headers.
 * This wrapper uses openjdk.nashorn engine.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 */
public class JsOpenJdkNashornHeaders extends CommonJsHeaders implements AbstractOpenJdkNashornJsObject {

    public JsOpenJdkNashornHeaders(Map wrapped, HttpServletResponse response) {

        super(wrapped, response);
    }

    @Override
    public Object getMember(String name) {

        Object member = super.getMember(name);
        if (member != null) {
            return member;
        } else {
            return AbstractOpenJdkNashornJsObject.super.getMember(name);
        }
    }

    @Override
    public void removeMember(String name) {

        boolean isRemoved = super.removeMemberObject(name);
        if (!isRemoved) {
            AbstractOpenJdkNashornJsObject.super.removeMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        boolean isSet = super.setMemberObject(name, value);
        if (!isSet) {
            AbstractOpenJdkNashornJsObject.super.setMember(name, value);
        }
    }
}
