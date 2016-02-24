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
 * encapsulates policy finder related data
 */
public class PolicyFinderDataHolder {

    private String moduleName;

    private String className;

    private String[] policyIdentifiers = new String[0];

    private String combiningAlgorithm;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String[] getPolicyIdentifiers() {
        return Arrays.copyOf(policyIdentifiers, policyIdentifiers.length);
    }

    public void setPolicyIdentifiers(String[] policyIdentifiers) {
        if (policyIdentifiers != null) {
            this.policyIdentifiers = Arrays.copyOf(policyIdentifiers, policyIdentifiers.length);
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCombiningAlgorithm() {
        return combiningAlgorithm;
    }

    public void setCombiningAlgorithm(String combiningAlgorithm) {
        this.combiningAlgorithm = combiningAlgorithm;
    }
}
