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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Inbound authentication configuration of an application.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "InboundAuthenticationConfig")
public class InboundAuthenticationConfig implements Serializable {

    private static final long serialVersionUID = 2768674144259414077L;

    @XmlElementWrapper(name = "InboundAuthenticationRequestConfigs")
    @XmlElement(name = "InboundAuthenticationRequestConfig")
    private InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs = new
            InboundAuthenticationRequestConfig[0];

    /*
     * <InboundAuthenticationConfig>
     * <InboundAuthenticationRequestConfigs></InboundAuthenticationRequestConfigs>
     * </InboundAuthenticationConfig>
     */
    public static InboundAuthenticationConfig build(OMElement inboundAuthenticationConfigOM) {

        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();

        if (inboundAuthenticationConfigOM == null) {
            return inboundAuthenticationConfig;
        }

        Iterator<?> iter = inboundAuthenticationConfigOM.getChildElements();

        while (iter.hasNext()) {

            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("InboundAuthenticationRequestConfigs".equals(elementName)) {

                Iterator<?> inboundAuthenticationRequestConfigsIter = element.getChildElements();

                List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigsArrList;
                inboundAuthenticationRequestConfigsArrList = new ArrayList<InboundAuthenticationRequestConfig>();

                if (inboundAuthenticationRequestConfigsIter != null) {

                    while (inboundAuthenticationRequestConfigsIter.hasNext()) {
                        OMElement inboundAuthenticationRequestConfigsElement;
                        inboundAuthenticationRequestConfigsElement = (OMElement) inboundAuthenticationRequestConfigsIter
                                .next();
                        InboundAuthenticationRequestConfig authReqConfig;
                        authReqConfig = InboundAuthenticationRequestConfig
                                .build(inboundAuthenticationRequestConfigsElement);
                        if (authReqConfig != null) {
                            inboundAuthenticationRequestConfigsArrList.add(authReqConfig);
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(inboundAuthenticationRequestConfigsArrList)) {
                    InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigsArr
                            = inboundAuthenticationRequestConfigsArrList
                            .toArray(new InboundAuthenticationRequestConfig[0]);
                    inboundAuthenticationConfig
                            .setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigsArr);
                }
            }
        }

        return inboundAuthenticationConfig;

    }

    /**
     * @return
     */
    public InboundAuthenticationRequestConfig[] getInboundAuthenticationRequestConfigs() {
        return inboundAuthenticationRequestConfigs;
    }

    /**
     * @param inboundAuthenticationRequestConfigs
     */
    public void setInboundAuthenticationRequestConfigs(
            InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfigs) {
        this.inboundAuthenticationRequestConfigs = inboundAuthenticationRequestConfigs;
    }
}
