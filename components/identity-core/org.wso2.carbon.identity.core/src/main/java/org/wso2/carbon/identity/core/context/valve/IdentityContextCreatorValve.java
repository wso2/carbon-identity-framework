/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.context.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.context.IdentityContext;

import java.io.IOException;

import javax.servlet.ServletException;

public class IdentityContextCreatorValve extends ValveBase {

    private static Log LOG = LogFactory.getLog(IdentityContextCreatorValve.class);

    public IdentityContextCreatorValve() {
        // Enable async support to handle asynchronous requests, allowing non-blocking operations
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            initIdentityContext();
            getNext().invoke(request, response);
        } catch (Exception e) {
            LOG.error("Could not handle request: " + request.getRequestURI(), e);
        } finally {
            // This will destroy the identity context data holder on the current thread.
            IdentityContext.destroyCurrentContext();
        }
    }

    public void initIdentityContext() {

        IdentityContext.getThreadLocalIdentityContext();
    }
}
