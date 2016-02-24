/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

public class CommonAuthRequestWrapper extends HttpServletRequestWrapper {

    private Map extraParameters;

    public CommonAuthRequestWrapper(HttpServletRequest request) {

        super(request);
        extraParameters = new HashMap();
    }

    public String getParameter(String name) {

        if (extraParameters.containsKey(name)) {
            return (String) extraParameters.get(name);
        } else {
            return super.getParameter(name);
        }
    }

    public void setParameter(String name, String value) {

        extraParameters.put(name, value);
    }

}
