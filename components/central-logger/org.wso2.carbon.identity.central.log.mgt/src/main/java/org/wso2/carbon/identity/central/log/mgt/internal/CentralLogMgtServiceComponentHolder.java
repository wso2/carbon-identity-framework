/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.central.log.mgt.internal;

import org.wso2.carbon.identity.event.services.IdentityEventService;

/**
 * Service component holder class for central logger.
 */
public class CentralLogMgtServiceComponentHolder {

    private static CentralLogMgtServiceComponentHolder centralLogMgtServiceComponentHolder =
            new CentralLogMgtServiceComponentHolder();

    private CentralLogMgtServiceComponentHolder() {

    }

    public static CentralLogMgtServiceComponentHolder getInstance() {

        return centralLogMgtServiceComponentHolder;
    }

    private IdentityEventService identityEventService;

    /**
     * Set identity event service.
     *
     * @param identityEventService Identity Event Service.
     */
    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }

    /**
     * Return identity event service.
     *
     * @return Identity Event Service.
     */
    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }
}
