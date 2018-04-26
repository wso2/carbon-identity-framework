/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.adaptive.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *  Interface to query from siddhi app runtime.
 */
public class QueryInterface {

    private static final Log log = LogFactory.getLog(QueryInterface.class);
    private Map<String, SiddhiAppRuntime> siddhiAppRunTimeMap;

    public QueryInterface(Map<String, SiddhiAppRuntime> siddhiAppRunTimeMap) {

        this.siddhiAppRunTimeMap = siddhiAppRunTimeMap;
    }

    public List<Event> query(String appName, String query) {

        SiddhiAppRuntime appRunTime = siddhiAppRunTimeMap.get(appName);
        if (appRunTime == null) {
            log.error("Siddhi app: " + appName + " is not registered/started.");
        } else {
            Event[] result = appRunTime.query(query);
            if (result == null) {
                return Collections.emptyList();
            } else {
                return Arrays.asList(result);
            }
        }

        return Collections.emptyList();
    }
}
