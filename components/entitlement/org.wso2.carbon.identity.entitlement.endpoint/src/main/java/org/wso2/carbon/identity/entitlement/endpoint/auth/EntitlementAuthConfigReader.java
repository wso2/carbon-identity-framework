/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EntitlementAuthConfigReader {

    private static Log logger = LogFactory.getLog(EntitlementAuthConfigReader.class);

    public List<EntitlementAuthenticationHandler> buildSCIMAuthenticators() {

//        IdentityConfigParser identityConfig = IdentityConfigParser.getInstance();
        try {
//            OMElement scimElem = identityConfig.getConfigElement(SCIMProviderConstants.ELEMENT_NAME_SCIM);
//            if(scimElem != null) {
//                OMElement scimAuthElement = scimElem.getFirstChildWithName(
//                        new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
//                                SCIMProviderConstants.ELEMENT_NAME_SCIM_AUTHENTICATORS));
//                //iterate through authenticators and build authenticators list
//                Iterator<OMElement> authenticators = scimAuthElement.getChildrenWithName(new QName(
//                        SCIMProviderConstants.ELEMENT_NAME_AUTHENTICATOR));
//                List<EntitlementAuthenticationHandler> SCIMAuthHandlers = new ArrayList<EntitlementAuthenticationHandler>();
//                if (authenticators != null) {
//                    while (authenticators.hasNext()) {
//                        OMElement authenticatorElement = authenticators.next();
//                        //read the authenticator class name
//                        String authenticatorClassName = authenticatorElement.getAttributeValue(new QName(
//                                SCIMProviderConstants.ATTRIBUTE_NAME_CLASS));
//                        //initialize the authenticatorElement
//                        Class authenticatorClass = Class.forName(authenticatorClassName);
//                        EntitlementAuthenticationHandler authHandler = (EntitlementAuthenticationHandler)
//                                authenticatorClass.newInstance();
//
//                        //read the properties in the authenticator element and set them in the authenticator.
//                        Iterator<OMElement> propertyElements = authenticatorElement.getChildrenWithName(new QName(
//                                SCIMProviderConstants.ELEMENT_NAME_PROPERTY));
//                        if (propertyElements != null) {
//                            Map<String, String> properties = new HashMap<String, String>();
//                            while (propertyElements.hasNext()) {
//                                OMElement propertyElement = propertyElements.next();
//                                String attributeName = propertyElement.getAttributeValue(new QName(
//                                        SCIMProviderConstants.ATTRIBUTE_NAME_NAME));
//                                String attributeValue = propertyElement.getText();
//                                properties.put(attributeName, attributeValue);
//                            }
//                            authHandler.setProperties(properties);
//                        }
//                        SCIMAuthHandlers.add(authHandler);
//                    }
//                    return SCIMAuthHandlers;
//                }
//            }

            //temporarily adding the authenticator classes hardcoded
            //will be added to the identity xml lateron

            BasicAuthHandler basicAuth = new BasicAuthHandler();
            HashMap<String, String> basicAuthProps = new HashMap<>();
            basicAuthProps.put("Priority", "5");
            basicAuth.setProperties(basicAuthProps);

            OAuthHandler oAuth = new OAuthHandler();
            HashMap<String, String> oAuthProps = new HashMap<>();
            oAuthProps.put("Priority", "10");
            oAuthProps.put("AuthorizationServer", "local://services");
            oAuth.setProperties(oAuthProps);

            List<EntitlementAuthenticationHandler> entitlementAuthHandlers = new ArrayList<EntitlementAuthenticationHandler>();
            entitlementAuthHandlers.add(basicAuth);
            entitlementAuthHandlers.add(oAuth);

            return entitlementAuthHandlers;

//        } catch (ClassNotFoundException e) {
//            //we just log the exception and continue loading other authenticators.
//            logger.error("Error in loading the authenticator class...", e);
//        } catch (InstantiationException e) {
//            logger.error("Error while instantiating the authenticator..", e);
//        } catch (IllegalAccessException e) {
//            logger.error("Error while instantiating the authenticator..", e);
        } catch (Exception e) {
            logger.error("Error in loading the authenticator class...", e);
        }
        return Collections.emptyList();
    }
}
