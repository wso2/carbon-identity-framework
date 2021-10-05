/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.session.extender;

import org.json.JSONObject;

/**
 * Test constants for Session Extender endpoint.
 */
public final class SessionExtenderTestConstants {

    public static final String SESSION_EXTENDER_ENDPOINT_URL = "https://localhost:9443/identity/extend-session/";

    public static final String IDP_SESSION_KEY = "d400af9434f4bfc2bd7130dae1ff40d74dc115f0ab9738f4ebb9d665c9c432f90";

    public static final String SESSION_COOKIE_NAME = "commonAuthId";

    public static final String SESSION_COOKIE_VALUE = "882bd705d-388c-4965-a27e-0eef04ceea0a";

    public static final String EXCEPTION_ERROR_CODE = "ISE-60001";

    public static final String EXCEPTION_MESSAGE = "Unexpected server error.";

    public static final String EXCEPTION_DESCRIPTION = "Unexpected server error.";

    public static final String TRACE_ID = "faaabef8-df76-408a-aa54-808858c250be";

    public static final String ERROR_RESPONSE_BODY =
            new JSONObject().put("code", EXCEPTION_ERROR_CODE).put("message", EXCEPTION_MESSAGE).
                    put("description", EXCEPTION_DESCRIPTION).put("traceId", TRACE_ID).toString();

    public static final String TENANT_DOMAIN = "carbon.super";
}
