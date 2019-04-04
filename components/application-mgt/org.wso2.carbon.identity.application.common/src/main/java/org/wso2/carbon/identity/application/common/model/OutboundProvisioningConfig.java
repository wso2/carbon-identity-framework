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

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "OutboundProvisioningConfig")
public class OutboundProvisioningConfig implements Serializable {

    private static final long serialVersionUID = 3669233357817378229L;

    @XmlElementWrapper(name="ProvisioningIdentityProviders")
    @XmlElement(name = "IdentityProvider")
    private IdentityProvider[] provisioningIdentityProviders = new IdentityProvider[0];

    @XmlElementWrapper(name="ProvisionByRoleList")
    @XmlElement(name = "ProvisionByRole")
    private String[] provisionByRoleList;

    /*
     * <OutboundProvisioningConfig> <ProvisioningIdentityProviders></ProvisioningIdentityProviders>
     * <ProvisionByRoleList></ProvisionByRoleList> </OutboundProvisioningConfig>
     */
    public static OutboundProvisioningConfig build(OMElement outboundProvisioningConfigOM) {
        OutboundProvisioningConfig outboundProvisioningConfig = new OutboundProvisioningConfig();

        Iterator<?> iter = outboundProvisioningConfigOM.getChildElements();

        while (iter.hasNext()) {
            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("ProvisioningIdentityProviders".equals(elementName)) {

                Iterator<?> provisioningIdentityProvidersIter = element.getChildElements();
                List<IdentityProvider> provisioningIdentityProvidersArrList = new ArrayList<IdentityProvider>();

                if (provisioningIdentityProvidersIter != null) {
                    while (provisioningIdentityProvidersIter.hasNext()) {
                        OMElement provisioningIdentityProvidersElement = (OMElement) (provisioningIdentityProvidersIter
                                .next());
                        IdentityProvider idp = IdentityProvider
                                .build(provisioningIdentityProvidersElement);
                        if (idp != null) {
                            provisioningIdentityProvidersArrList.add(idp);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(provisioningIdentityProvidersArrList)) {
                    IdentityProvider[] provisioningIdentityProvidersArr = provisioningIdentityProvidersArrList
                            .toArray(new IdentityProvider[0]);
                    outboundProvisioningConfig
                            .setProvisioningIdentityProviders(provisioningIdentityProvidersArr);
                }
            } else if ("ProvisionByRoleList".equals(elementName)) {

                Iterator<?> provisionByRoleListIter = element.getChildElements();
                List<String> provisionByRoleListArrList = new ArrayList<String>();

                if (provisionByRoleListIter != null) {
                    while (provisionByRoleListIter.hasNext()) {
                        OMElement provisionByRoleListElement = (OMElement) (provisionByRoleListIter
                                .next());
                        if (provisionByRoleListElement.getText() != null) {
                            provisionByRoleListArrList.add(provisionByRoleListElement.getText());
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(provisionByRoleListArrList)) {
                    String[] provisionByRoleListArr = provisionByRoleListArrList
                            .toArray(new String[0]);
                    outboundProvisioningConfig.setProvisionByRoleList(provisionByRoleListArr);
                }
            }
        }

        return outboundProvisioningConfig;
    }

    /**
     * @return
     */
    public IdentityProvider[] getProvisioningIdentityProviders() {
        return provisioningIdentityProviders;
    }

    /**
     * @param provisioningIdentityProviders
     */
    public void setProvisioningIdentityProviders(IdentityProvider[] provisioningIdentityProviders) {
        if (provisioningIdentityProviders == null) {
            return;
        }
        Set<IdentityProvider> propertySet = new HashSet<IdentityProvider>(Arrays.asList(provisioningIdentityProviders));
        this.provisioningIdentityProviders = propertySet.toArray(new IdentityProvider[propertySet.size()]);
    }

    /**
     * @return
     */
    public String[] getProvisionByRoleList() {
        return provisionByRoleList;
    }

    /**
     * @param provisionByRoleList
     */
    public void setProvisionByRoleList(String[] provisionByRoleList) {
        this.provisionByRoleList = provisionByRoleList;
    }
}
