/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IdentityMgtServiceDataHolder {

    private static IdentityMgtServiceDataHolder instance = new IdentityMgtServiceDataHolder();

    private static Log log = LogFactory.getLog(IdentityMgtServiceDataHolder.class);

    private boolean userSessionMappingEnabled;

    private IdentityMgtServiceDataHolder() {

    }

    public static IdentityMgtServiceDataHolder getInstance() {

        return instance;
    }

    /**
     * Is user session mapping enabled.
     *
     * @return return true if user session mapping enabled.
     */
    public boolean isUserSessionMappingEnabled() {

        return this.userSessionMappingEnabled;
    }

    /**
     * Set user session mapping enabled.
     *
     * @param userSessionMappingEnabled
     */
    public void setUserSessionMappingEnabled(boolean userSessionMappingEnabled) {

        if (log.isDebugEnabled()) {
            if (userSessionMappingEnabled) {
                log.debug("User session mapping enabled for server.");
            } else {
                log.debug("User session mapping not enabled for server.");
            }
        }

        this.userSessionMappingEnabled = userSessionMappingEnabled;
    }
}
