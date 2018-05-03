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

package org.wso2.carbon.identity.adaptive.auth.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.adaptive.auth.internal.AdaptiveDataHolder;

import java.util.Map;

/**
 * Publish event function.
 */
public class PublishEventFunctionImpl implements PublishEventFunction {

    private static final Log log = LogFactory.getLog(PublishEventFunctionImpl.class);

    @Override
    public void publishEvent(String siddhiAppName, String streamName, Map<String, Object> payloadData) {

        AdaptiveDataHolder.getInstance().getEventPublisher().publish(siddhiAppName, streamName, payloadData);
    }
}
