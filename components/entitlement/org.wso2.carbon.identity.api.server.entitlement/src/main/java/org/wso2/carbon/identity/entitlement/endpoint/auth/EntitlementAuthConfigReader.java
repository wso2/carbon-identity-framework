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

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Initiating authenticator classes
 * </p>
 */
public class EntitlementAuthConfigReader {

    private static Log logger = LogFactory.getLog(EntitlementAuthConfigReader.class);

    public List<EntitlementAuthenticationHandler> buildEntitlementAuthenticators() {

        try {
            BasicAuthHandler basicAuth = new BasicAuthHandler();
            HashMap<String, String> basicAuthProps = new HashMap<String, String>();
            basicAuthProps.put("Priority", "5");
            basicAuth.setProperties(basicAuthProps);

            List<EntitlementAuthenticationHandler> entitlementAuthHandlers = new ArrayList<EntitlementAuthenticationHandler>();
            entitlementAuthHandlers.add(basicAuth);

            /**
             * TODO : Remove hardcoded Authenticator initializing and read authenticator
             *        from identity config
             */

            return entitlementAuthHandlers;
        } catch (Exception e) {
            logger.error("Error in loading the authenticator class...", e);
        }
        return Collections.emptyList();
    }
}
