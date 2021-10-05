/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store;

/**
 * This class is used to pass the data to and from the SessionDataStore
 */
public class SessionContextDO {

    private String key;

    private String type;

    private Object entry;

    private long nanoTime;

    private int tenantId;

    public SessionContextDO(String key, String type, Object entry, long nanoTime) {
        this.type = type;
        this.key = key;
        this.entry = entry;
        this.nanoTime = nanoTime;
    }

    public SessionContextDO(String key, String type, Object entry, long nanoTime, int tenantId) {
        this.type = type;
        this.key = key;
        this.entry = entry;
        this.nanoTime = nanoTime;
        this.tenantId = tenantId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getEntry() {
        return entry;
    }

    public void setEntry(Object entry) {
        this.entry = entry;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public void setNanoTime(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
