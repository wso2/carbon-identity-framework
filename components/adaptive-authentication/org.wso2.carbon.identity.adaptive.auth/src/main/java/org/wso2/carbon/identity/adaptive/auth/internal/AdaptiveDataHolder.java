/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.adaptive.auth.internal;

import org.wso2.carbon.identity.adaptive.auth.EmbeddedSiddhiEngine;
import org.wso2.carbon.identity.adaptive.auth.QueryInterface;
import org.wso2.carbon.identity.adaptive.auth.SiddhiEventPublisher;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Data holder for adaptive authentication component.
 */
public class AdaptiveDataHolder {

    private SiddhiManager siddhiManager;
    private QueryInterface queryInterface;
    private SiddhiEventPublisher eventPublisher;
    private EmbeddedSiddhiEngine siddhiEngine;

    private static AdaptiveDataHolder instance = new AdaptiveDataHolder();

    public static AdaptiveDataHolder getInstance() {

        return instance;
    }

    public SiddhiManager getSiddhiManager() {

        return siddhiManager;
    }

    public void setSiddhiManager(SiddhiManager siddhiManager) {

        this.siddhiManager = siddhiManager;
    }

    public QueryInterface getQueryInterface() {

        return queryInterface;
    }

    public void setQueryInterface(QueryInterface queryInterface) {

        this.queryInterface = queryInterface;
    }

    public SiddhiEventPublisher getEventPublisher() {

        return eventPublisher;
    }

    public void setEventPublisher(SiddhiEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    public EmbeddedSiddhiEngine getSiddhiEngine() {

        return siddhiEngine;
    }

    public void setSiddhiEngine(EmbeddedSiddhiEngine siddhiEngine) {

        this.siddhiEngine = siddhiEngine;
    }
}
