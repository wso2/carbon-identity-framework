/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.mail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NotificationData {

    private String sendTo;
    private String sendFrom;
    private Map<String, String> tagData;

    public NotificationData() {
        this.tagData = new HashMap<String, String>();
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    public void setSendFrom(String sendFrom) {
        this.sendFrom = sendFrom;
    }

    public String getTagData(String key) {
        return this.tagData.get(key);
    }

    public void setTagData(String key, String value) {
        this.tagData.put(key, value);
    }

    public Set<String> getTagKeys() {
        return tagData.keySet();
    }

    public Map<String, String> getTagsData() {
        return this.tagData;
    }

}
