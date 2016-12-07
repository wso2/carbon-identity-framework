/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.util;

import org.wso2.carbon.identity.framework.FrameworkConstants;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

import java.util.UUID;

public class FrameworkUtil {

    public static int comparePriory(int priority1, int priority2) {
        if (priority1 > priority2) {
            return 1;
        } else if (priority1 < priority2) {
            return -1;
        } else {
            return 0;
        }
    }

    public static String generateSessionIdentifier() {
        return UUID.randomUUID().toString();
    }


    public static void addSessionIdentifierToContext(IdentityMessageContext context, String sessionId) {
        context.addParameter(FrameworkConstants.SESSION_ID, sessionId);
    }

    public static String getSessionIdentifier(IdentityMessageContext context) {
        return String.valueOf(context.getParameter(FrameworkConstants.SESSION_ID));
    }
}
