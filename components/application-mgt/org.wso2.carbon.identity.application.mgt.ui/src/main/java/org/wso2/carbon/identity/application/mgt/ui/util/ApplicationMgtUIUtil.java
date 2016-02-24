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

package org.wso2.carbon.identity.application.mgt.ui.util;

import org.wso2.carbon.identity.application.mgt.ui.ApplicationBean;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApplicationMgtUIUtil {

    private static final String SP_UNIQUE_ID_MAP = "spUniqueIdMap";

    /**
     * Get related application bean from the session.
     *
     * @param session HTTP Session.
     * @param spName  Service provider name.
     * @return ApplicationBean
     */
    public static ApplicationBean getApplicationBeanFromSession(HttpSession session, String spName) {

        Map<String, UUID> spUniqueIdMap;

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            spUniqueIdMap = new HashMap<>();
            session.setAttribute(SP_UNIQUE_ID_MAP, spUniqueIdMap);
        } else {
            spUniqueIdMap = (HashMap<String, UUID>)session.getAttribute(SP_UNIQUE_ID_MAP);
        }

        if (spUniqueIdMap.get(spName) == null) {
            ApplicationBean applicationBean = new ApplicationBean();
            UUID uuid = UUID.randomUUID();
            spUniqueIdMap.put(spName, uuid);
            session.setAttribute(uuid.toString(), applicationBean);
        }
        return (ApplicationBean) session.getAttribute(spUniqueIdMap.get(spName).toString());
    }

    /**
     * Remove related application bean from the session.
     *
     * @param session Http Session.
     * @param spName  Service provider name.
     */
    public static void removeApplicationBeanFromSession(HttpSession session, String spName) {

        if (session.getAttribute(SP_UNIQUE_ID_MAP) == null) {
            return;
        }
        Map<String, UUID> spUniqueIdMap = (HashMap<String, UUID>)session.getAttribute(SP_UNIQUE_ID_MAP);

        if (spUniqueIdMap.get(spName) == null) {
            return;
        }
        session.removeAttribute(spUniqueIdMap.get(spName).toString());
        spUniqueIdMap.remove(spName);
    }
}