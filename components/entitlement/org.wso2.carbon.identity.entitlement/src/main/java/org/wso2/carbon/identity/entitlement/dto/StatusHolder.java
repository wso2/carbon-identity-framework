/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.dto;

import org.wso2.carbon.context.CarbonContext;

/**
 *
 */
public class StatusHolder {

    /**
     * Status type
     */
    private String type;
    /**
     * key to identify status. basically policy Id
     */
    private String key;

    /**
     * basically policy version
     */
    private String version;

    /**
     * whether this is success status or not
     */
    private boolean success;

    /**
     * the user who is involved with this
     */
    private String user;

    /**
     * target
     */
    private String target;

    /**
     * target action
     */
    private String targetAction;

    /**
     * time instance
     */
    private String timeInstance;

    /**
     * message
     */
    private String message;


    public StatusHolder(String type, String key, String version, String target,
                        String targetAction, boolean success, String message) {
        this.type = type;
        this.key = key;
        this.user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        this.message = message;
        this.version = version;
        this.target = target;
        this.targetAction = targetAction;
        this.success = success;
        this.timeInstance = Long.toString(System.currentTimeMillis());
    }

    public StatusHolder(String type, String key, String version, String target, String targetAction) {
        this.type = type;
        this.key = key;
        this.version = version;
        this.target = target;
        this.targetAction = targetAction;
        this.user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        this.success = true;
        this.timeInstance = Long.toString(System.currentTimeMillis());
    }

    public StatusHolder(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTimeInstance() {
        return timeInstance;
    }

    public void setTimeInstance(String timeInstance) {
        this.timeInstance = timeInstance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }
}
