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
package org.wso2.carbon.identity.entitlement.ui.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants;

import javax.xml.namespace.QName;


/**
 *  @deprecated  As this moved to org.wso2.carbon.identity.entitlement.common
 */
@Deprecated
public class ClientUtil {

    /**
     * Helper method to extract the boolean response
     *
     * @param xmlstring XACML resource as String
     * @return Decision
     * @throws Exception if fails
     */
    public static String getStatus(String xmlstring) throws Exception {

        OMElement response = null;
        OMElement result = null;
        OMElement decision = null;
        response = AXIOMUtil.stringToOM(xmlstring);

        OMNamespace nameSpace = response.getNamespace();

        if (nameSpace != null) {
            result = response.getFirstChildWithName(new QName(nameSpace.getNamespaceURI(), "Result"));
        } else {
            result = response.getFirstElement();
        }
        if (result != null) {
            if (nameSpace != null) {
                decision = result.getFirstChildWithName(new QName(nameSpace.getNamespaceURI(), "Decision"));
            } else {
                decision = result.getFirstChildWithName(new QName("Decision"));
            }
            if (decision != null) {
                return decision.getText();
            }
        }

        return "Invalid Status";
    }

    public static String[] doPagingForStrings(int pageNumber, int itemsPerPageInt, String[] names) {

        String[] returnedSubscriberNameSet;

        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        if (itemsPerPageInt < names.length) {
            returnedSubscriberNameSet = new String[itemsPerPageInt];
        } else {
            returnedSubscriberNameSet = new String[names.length];
        }
        for (int i = startIndex, j = 0; i < endIndex && i < names.length; i++, j++) {
            returnedSubscriberNameSet[j] = names[i];
        }

        return returnedSubscriberNameSet;
    }

    public static StatusHolder[] doModuleStatusHoldersPaging(int pageNumber,
                                                             StatusHolder[] moduleStatusHolderSet) {

        int itemsPerPageInt = EntitlementPolicyConstants.DEFAULT_ITEMS_PER_PAGE;
        StatusHolder[] returnedModuleStatusHolderSet;

        int startIndex = pageNumber * itemsPerPageInt;
        int endIndex = (pageNumber + 1) * itemsPerPageInt;
        if (itemsPerPageInt < moduleStatusHolderSet.length) {
            returnedModuleStatusHolderSet = new StatusHolder[itemsPerPageInt];
        } else {
            returnedModuleStatusHolderSet = new StatusHolder[moduleStatusHolderSet.length];
        }
        for (int i = startIndex, j = 0; i < endIndex && i < moduleStatusHolderSet.length; i++, j++) {
            returnedModuleStatusHolderSet[j] = moduleStatusHolderSet[i];
        }

        return returnedModuleStatusHolderSet;
    }

}
