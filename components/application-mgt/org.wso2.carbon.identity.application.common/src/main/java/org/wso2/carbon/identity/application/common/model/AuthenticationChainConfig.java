/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Configuration for the quthentication chain in SequenceConfig.
 *
 */
public class AuthenticationChainConfig implements Serializable {

    private static final long serialVersionUID = 497647508006862449L;
    private static final Log log = LogFactory.getLog(AuthenticationChainConfig.class);
    private static final String AUTHENTICATION_STEPS = "AuthenticationSteps";
    private static final QName ATTRIBUTE_NAME = new QName(null, "name");
    private static final QName ATTRIBUTE_ACR_VALUES = new QName(null, "acrValues");

    private String name;
    private String[] acr;
    private AuthenticationStep[] stepConfigs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAcr() {
        return acr;
    }

    public AuthenticationStep[] getStepConfigs() {
        return stepConfigs;
    }

    public void setAcr(String[] acr) {
        this.acr = acr;
    }

    public void setStepConfigs(AuthenticationStep[] stepConfigs) {
        this.stepConfigs = stepConfigs;
    }

    /**
     *
     * Builds the AuthenticationChainConfig with the XML fragment.
     *
     * <AuthenticationChains>
     *		<AuthenticationChain name="chain1">
     *		  <AuthenticationSteps> ... *
     *
     *
     */
    public static AuthenticationChainConfig build(OMElement authenticationChainOM) {
        AuthenticationChainConfig chainConfig = new AuthenticationChainConfig();

        Iterator<?> iter = authenticationChainOM.getChildElements();

        String name = authenticationChainOM.getAttributeValue(ATTRIBUTE_NAME);
        String acrValues = authenticationChainOM.getAttributeValue(ATTRIBUTE_ACR_VALUES);
        chainConfig.setAcr(decodeAcr(acrValues));
        chainConfig.setName(name);

        List<AuthenticationStep> authenticationStepList = new ArrayList<>();
        while (iter.hasNext()) {
            OMElement member = (OMElement) iter.next();
            switch (member.getLocalName()) {
            case AUTHENTICATION_STEPS:
                Iterator<?> stepsIter = member.getChildElements();
                while (stepsIter.hasNext()) {
                    OMElement stepElem = (OMElement) stepsIter.next();
                    AuthenticationStep authenticationStep = AuthenticationStep.build(stepElem);
                    authenticationStepList.add(authenticationStep);
                }
                break;
            default:
                log.error("Unsupported element in AuthenticationChain. element: " + member.getLocalName());
            }
        }
        chainConfig
                .setStepConfigs(authenticationStepList.toArray(new AuthenticationStep[authenticationStepList.size()]));
        return chainConfig;
    }

    private static String[] decodeAcr(String acrValues) {
        if (acrValues == null || acrValues.isEmpty()) {
            return new String[0];
        }
        String[] strings = acrValues.split(",|\\s");
        List<String> acrList = new ArrayList<>();
        for (String s : strings) {
            String s1 = s.trim();
            if (!s.isEmpty()) {
                acrList.add(s1);
            }
        }
        return acrList.toArray(new String[acrList.size()]);
    }
}
