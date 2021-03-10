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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseHeaders;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;


/**
 * Javascript wrapper for Java level HashMap of HTTP headers for Nashorn Execution.
 * This provides controlled access to HTTPServletResponse object's headers via provided javascript native syntax.
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime.
 */
public class NashornJsHeaders extends JsBaseHeaders implements AbstractJsObject {

    public NashornJsHeaders(Map wrapped, HttpServletResponse response) {

        super(wrapped, response);
    }

    @Override
    public Object getMember(String name) {

        if (wrapped == null) {
            return AbstractJsObject.super.getMember(name);
        } else {
            return wrapped.get(name);
        }
    }

    @Override
    public void removeMember(String name) {

        if (wrapped == null) {
            AbstractJsObject.super.removeMember(name);
        } else {
            if (wrapped.containsKey(name)) {
                wrapped.remove(name);
            }
        }
    }

    @Override
    public void setMember(String name, Object value) {

        if (wrapped == null) {
            AbstractJsObject.super.setMember(name, value);
        } else {
            wrapped.put(name, value);
            //adds a new header to the response.
            response.addHeader(name, String.valueOf(value));
        }
    }
}
