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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Javascript wrapper for Java level HTTPServletRequest.
 * This wrapper uses jdk.nashorn engine.
 * This provides controlled access to HTTPServletRequest object via provided javascript native syntax.
 * e.g
 * var redirect_uri = context.request.params.redirect_uri
 * <p>
 * instead of
 * var userName = context.getRequest().getParameter("redirect_uri)
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * HTTPServletRequest.
 */
public class JsNashornServletRequest extends JsServletRequest implements AbstractJsObject {

    public JsNashornServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        super(wrapped);
    }
}

