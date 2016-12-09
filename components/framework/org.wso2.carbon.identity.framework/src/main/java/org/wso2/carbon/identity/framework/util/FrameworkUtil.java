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

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

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


    public static IdentityMessageContext mergeContext(IdentityMessageContext newContext,
                                                      IdentityMessageContext oldContext) {

        // Copy the data from old context
        newContext.setSessionDataKey(oldContext.getSessionDataKey());
        newContext.setInitialIdentityRequest(oldContext.getInitialIdentityRequest());
        newContext.setIdentityResponse(oldContext.getIdentityResponse());
        newContext.addParameters(oldContext.getParameters());

        // restore current state from the old context.
        newContext.setCurrentHandler(oldContext.getCurrentHandler());
        newContext.setCurrentHandlerStatus(oldContext.getCurrentHandlerStatus());

        return newContext;
    }
}
