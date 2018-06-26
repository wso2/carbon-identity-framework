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

package org.wso2.carbon.identity.application.authentication.framework.servlet;

import com.google.gson.Gson;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.LongWaitStatusRequest;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.LongWaitStatusResponse;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LongWaitStatusServlet extends HttpServlet {

    private static final long serialVersionUID = -3714283612680472526L;
    private static final String PROP_WAITING_ID = "waitingId";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (FrameworkUtils.getMaxInactiveInterval() == 0) {
            FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
        }
        String id = request.getParameter(PROP_WAITING_ID);
        if (id == null) {
            if (request.getContentType() != null && request.getContentType().startsWith
                    (FrameworkConstants.ContentTypes.TYPE_APPLICATION_JSON)) {
                Gson gson = new Gson();
                LongWaitStatusRequest longWaitStatusRequest = gson.fromJson(request.getReader(),
                                                                            LongWaitStatusRequest.class);
                id = longWaitStatusRequest.getWaitId();
            }
        }

        LongWaitStatusResponse longWaitResponse = new LongWaitStatusResponse();
        longWaitResponse.setWaitId(id);
        if (id == null) {
            longWaitResponse.setStatus(LongWaitStatus.Status.UNKNOWN.name());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            LongWaitStatusStoreService longWaitStatusStoreService =
                    FrameworkServiceDataHolder.getInstance().getLongWaitStatusStoreService();
            if (longWaitStatusStoreService == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                LongWaitStatus longWaitStatus = null;
                try {
                    longWaitStatus = longWaitStatusStoreService.getWait(id);
                } catch (FrameworkException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                if (longWaitStatus == null) {
                    longWaitResponse.setStatus(LongWaitStatus.Status.COMPLETED.name());
                } else {
                    if (longWaitStatus.getStatus() != null) {
                        if (longWaitStatus.getStatus() == LongWaitStatus.Status.UNKNOWN) {
                            longWaitResponse.setStatus(LongWaitStatus.Status.COMPLETED.name());
                        } else {
                            longWaitResponse.setStatus(longWaitStatus.getStatus().name());
                        }
                    } else {
                        longWaitResponse.setStatus(LongWaitStatus.Status.COMPLETED.name());
                    }
                }
            }
        }

        response.setContentType(FrameworkConstants.ContentTypes.TYPE_APPLICATION_JSON);
        String json = new Gson().toJson(longWaitResponse);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }
}
