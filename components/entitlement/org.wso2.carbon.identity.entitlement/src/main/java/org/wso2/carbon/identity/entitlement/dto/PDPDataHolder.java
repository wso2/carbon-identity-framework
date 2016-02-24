/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Arrays;

/**
 *
 */
public class PDPDataHolder {

    private String[] policyFinders = new String[0];

    private String[] pipAttributeFinders = new String[0];

    private String[] pipResourceFinders = new String[0];

    private boolean decisionCacheEnable;

    public String[] getPolicyFinders() {
        return Arrays.copyOf(policyFinders, policyFinders.length);
    }

    public void setPolicyFinders(String[] policyFinders) {
        this.policyFinders = Arrays.copyOf(policyFinders, policyFinders.length);
    }

    public String[] getPipAttributeFinders() {
        return Arrays.copyOf(pipAttributeFinders, pipAttributeFinders.length);
    }

    public void setPipAttributeFinders(String[] pipAttributeFinders) {
        this.pipAttributeFinders = Arrays.copyOf(pipAttributeFinders, pipAttributeFinders.length);
    }

    public String[] getPipResourceFinders() {
        return Arrays.copyOf(pipResourceFinders, pipResourceFinders.length);
    }

    public void setPipResourceFinders(String[] pipResourceFinders) {
        this.pipResourceFinders = Arrays.copyOf(pipResourceFinders, pipResourceFinders.length);
    }

    public boolean isDecisionCacheEnable() {
        return decisionCacheEnable;
    }

    public void setDecisionCacheEnable(boolean decisionCacheEnable) {
        this.decisionCacheEnable = decisionCacheEnable;
    }
}
