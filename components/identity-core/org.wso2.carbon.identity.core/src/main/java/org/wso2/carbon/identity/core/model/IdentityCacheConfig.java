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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.core.model;

public class IdentityCacheConfig {

    private IdentityCacheConfigKey identityCacheConfigKey;
    private boolean isEnabled;
    private int timeout;
    private int capacity;
    private boolean isDistributed = true;
    private boolean isTemporary = false;

    public IdentityCacheConfig(IdentityCacheConfigKey identityCacheConfigKey) {
        this.identityCacheConfigKey = identityCacheConfigKey;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public IdentityCacheConfigKey getIdentityCacheConfigKey() {
        return identityCacheConfigKey;
    }

    public boolean isDistributed() {
        return isDistributed;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    public void setDistributed(boolean isDistributed) {
        this.isDistributed = isDistributed;
    }
}
