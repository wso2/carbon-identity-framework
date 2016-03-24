/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class IdentityServlet extends HttpServlet {

    private InboundRequestManager manager = new InboundRequestManager();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) {

        InboundRequestFactory factory = getInboundRequestFactory(request, response);
        if (factory == null) {
            throw new FrameworkRuntimeException("No inbound request factory found to create the request");
        }

        InboundRequest inboundRequest = factory.create(request, response);

        InboundResponse inboundResponse = manager.process(inboundRequest);

        for(Map.Entry<String,String> entry:inboundResponse.getHeaders().entrySet()) {
            response.addHeader(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<String,Cookie> entry:inboundResponse.getCookies().entrySet()) {
            response.addCookie(entry.getValue());
        }
        if(StringUtils.isNotBlank(inboundResponse.getContentType())) {
            response.setContentType(inboundResponse.getContentType());
        }
        if (inboundResponse.getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            try {
                sendRedirect(response, inboundResponse);
            } catch (IOException ex) {
                throw new FrameworkRuntimeException("Error occurred while redirecting response", ex);
            }
        } else {
            response.setStatus(inboundResponse.getStatusCode());
            try {
                PrintWriter out = response.getWriter();
                if(StringUtils.isNotBlank(inboundResponse.getBody())) {
                    out.print(inboundResponse.getBody());
                }
            } catch (IOException e) {
                throw FrameworkRuntimeException.error("Error occurred while getting Response writer object", e);
            }
        }
    }

    private InboundRequestFactory getInboundRequestFactory(HttpServletRequest request, HttpServletResponse response) {

        List<InboundRequestFactory> factories = FrameworkServiceDataHolder.getInstance().getInboundRequestFactories();

        for (InboundRequestFactory requestBuilder : factories) {
            if (requestBuilder.canHandle(request, response)) {
                return requestBuilder;
            }
        }
        return null;
    }

    private void sendRedirect(HttpServletResponse response, InboundResponse inboundResponse) throws IOException {

        String queryParams = IdentityUtil.buildQueryString(inboundResponse.getParameters());
        response.sendRedirect(inboundResponse.getRedirectURL() + queryParams);
    }

}
