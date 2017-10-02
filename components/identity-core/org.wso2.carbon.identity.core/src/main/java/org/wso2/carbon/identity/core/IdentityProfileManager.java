/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.user.core.UserRealm;

public class IdentityProfileManager {
    private static Log log = LogFactory.getLog(IdentityClaimManager.class);
    // Maintains a single instance of UserStore.
    private static IdentityProfileManager profileManager;
    // To enable attempted thread-safety using double-check locking
    private static Object lock = new Object();
    // The effective realm instance used to extract user information.
    private UserRealm realm;

    // Making the class singleton
    private IdentityProfileManager() throws IdentityException {
    }

    public static IdentityProfileManager getInstance() throws IdentityException {

        // Enables attempted thread-safety using double-check locking
        if (profileManager == null) {
            synchronized (lock) {
                if (profileManager == null) {
                    profileManager = new IdentityProfileManager();
                    if (log.isDebugEnabled()) {
                        log.debug("IdentityClaimManager singleton instance created successfully");
                    }
                }
            }
        }
        return profileManager;
    }

    /**
     * Returns user realm set for IdentityProfileManager.
     *
     * @return user realm
     */
    public UserRealm getRealm() {
        return realm;
    }

    /**
     * Set user realm for IdentityProfileManager.
     *
     * @param realm user realm to be set
     */
    public void setRealm(UserRealm realm) {
        this.realm = realm;
        if (log.isDebugEnabled()) {
            if (realm != null) {
                log.debug("IdentityProfileManager UserRealm set successfully: "
                        + realm.getClass().getName());
            }
        }
    }

}
