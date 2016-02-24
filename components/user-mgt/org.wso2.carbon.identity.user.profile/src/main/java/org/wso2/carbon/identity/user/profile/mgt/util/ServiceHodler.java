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

package org.wso2.carbon.identity.user.profile.mgt.util;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserRealm;

import javax.servlet.http.HttpServletRequest;

public class ServiceHodler {




    private static final Log log = LogFactory.getLog(ServiceHodler.class);

    private static UserRealm internalUserStore;


    private ServiceHodler(){

    }

    public static void setInternalUserStore(UserRealm internalUserStore) {
        ServiceHodler.internalUserStore = internalUserStore;
    }

    public static UserRealm getUserRealm() throws UserProfileException {
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new UserProfileException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");

        UserRegistry registry =
                (UserRegistry) request.getSession().getAttribute(RegistryConstants.USER_REGISTRY);

        if (registry == null) {
            String msg = "User Registry instance is not found. " +
                    "Users have to login to retrieve a user registry instance for the tenant. ";
            log.error(msg);
            throw new UserProfileException(msg);
        }

        return registry.getUserRealm();
    }
}
