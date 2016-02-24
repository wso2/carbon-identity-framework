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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommonAuthResponseWrapper extends HttpServletResponseWrapper {

    private Map extraParameters;
    private HttpServletRequest request;
    private boolean isRedirect = false;
    private String redirectURL;

    public CommonAuthResponseWrapper(HttpServletResponse response) {

        super(response);
        extraParameters = new HashMap();
    }

    public CommonAuthResponseWrapper(HttpServletResponse response, HttpServletRequest request) {

        super(response);
        this.request = request;
        extraParameters = new HashMap();
    }

    @Override
    public void sendRedirect(String location) throws IOException {

        redirectURL = location;
        isRedirect = true;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public String getRedirectURL() {
        return redirectURL;
    }
}
