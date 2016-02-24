/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Encapsulates the entitled attributes that user has been entitled for
 */
public class EntitledAttributesDTO {

    private String resourceName;

    private String action;

    private String environment;

    private boolean allActions;

    private boolean allResources;

    private AttributeDTO[] attributeDTOs = new AttributeDTO[0];

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public boolean isAllActions() {
        return allActions;
    }

    public void setAllActions(boolean allActions) {
        this.allActions = allActions;
    }

    public boolean isAllResources() {
        return allResources;
    }

    public void setAllResources(boolean allResources) {
        this.allResources = allResources;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public AttributeDTO[] getAttributeDTOs() {
        return Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public void setAttributeDTOs(AttributeDTO[] attributeDTOs) {
        this.attributeDTOs = Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof EntitledAttributesDTO)) return false;
//
//        EntitledAttributesDTO that = (EntitledAttributesDTO) o;
//
//        if (allActions != that.allActions) return false;
//        if (allResources != that.allResources) return false;
//        if (action != null ? !action.equals(that.action) : that.action != null) return false;
//        if (environment != null ? !environment.equals(that.environment) : that.environment != null)
//            return false;
//        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        return super.hashCode();    //To change body of overridden methods use File | Settings | File Templates.
//    }
//
//    //    @Override
////    public int hashCode() {
////        int result = resourceName != null ? resourceName.hashCode() : 0;
////        result = 31 * result + (action != null ? action.hashCode() : 0);
////        result = 31 * result + (environment != null ? environment.hashCode() : 0);
////        result = 31 * result + (allActions ? 1 : 0);
////        result = 31 * result + (allResources ? 1 : 0);
////        return result;
////    }
}
