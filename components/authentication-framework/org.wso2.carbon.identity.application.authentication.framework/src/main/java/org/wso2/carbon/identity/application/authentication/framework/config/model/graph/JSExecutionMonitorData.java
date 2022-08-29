/*
 *  Copyright (c) 2021, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import java.io.Serializable;

/**
 * Holds the result of a script execution monitor.
 */
public class JSExecutionMonitorData implements Serializable {

    private static final long serialVersionUID = 1183234189933783699L;
    private long elapsedTime;
    private long consumedMemory;

    public JSExecutionMonitorData(long elapsedTime, long consumedMemory) {

        this.elapsedTime = elapsedTime;
        this.consumedMemory = consumedMemory;
    }

    public long getElapsedTime() {

        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {

        this.elapsedTime = elapsedTime;
    }

    public long getConsumedMemory() {

        return consumedMemory;
    }

    public void setConsumedMemory(long consumedMemory) {

        this.consumedMemory = consumedMemory;
    }
}
