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

package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationMethodNameTranslator;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Implementation of the AuthenticationMethodNameTranslator.
 * Currently reads mapping from identity.xml upon server startup.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework." +
                        "AuthenticationMethodNameTranslator",
                "service.scope=singleton"
        }
)
public class AuthenticationMethodNameTranslatorImpl implements AuthenticationMethodNameTranslator {

    private static final Log log = LogFactory.getLog(AuthenticationMethodNameTranslatorImpl.class);

    private static final String NS_CARBON = "http://wso2.org/projects/carbon/carbon.xml";
    private static final String CONTEXT_MAPPINGS = "AuthenticationContext";
    private static final String METHOD_REF_LOCAL_NAME = "MethodRef";

    private static final QName NIL_QNAME = new QName("http://www.w3.org/2001/XMLSchema-instance", "nil");
    private static final QName AUTH_CTX_QNAME = new QName(NS_CARBON, CONTEXT_MAPPINGS);
    private static final QName AMR_MAPPING_QNAME = new QName(NS_CARBON, "MethodRefs");
    private static final QName METHOD_REF_QNAME = new QName(NS_CARBON, METHOD_REF_LOCAL_NAME);
    private static final QName URI_ATTR_QNAME = new QName(null, "uri");
    private static final QName LEVEL_ATTR_QNAME = new QName(null, "level");
    private static final QName METHOD_ATTR_QNAME = new QName(null, "method");

    private Map<String, String> amrExternalToInternalMap = new HashMap<>();
    private Map<String, Set<String>> amrInternalToExternalMap = new HashMap<>();

    public void initializeConfigsWithServerConfig() {

        IdentityConfigParser configParser = IdentityConfigParser.getInstance();
        initializeConfigs(configParser.getConfigElement(CONTEXT_MAPPINGS));
    }

    void initializeConfigs(OMElement mappingsElement) {

        if (mappingsElement == null) {
            return;
        }
        if (mappingsElement.getLocalName().equals("Server")) {
            mappingsElement = mappingsElement.getFirstChildWithName(AUTH_CTX_QNAME);
        }

        OMElement amrRefsElement = mappingsElement.getFirstChildWithName(AMR_MAPPING_QNAME);
        if (amrRefsElement != null) {
            processAmrMappings(amrRefsElement);
        }

    }

    private void processAmrMappings(OMElement amrMapElement) {

        Iterator<OMElement> children = amrMapElement.getChildrenWithName(METHOD_REF_QNAME);
        for (int i = 0; children.hasNext(); i++) {
            OMElement child = children.next();
            processAmrEntry(child, amrInternalToExternalMap, amrExternalToInternalMap);
        }
    }

    private void processAmrEntry(OMElement amrEntryElement, Map<String, Set<String>> amrInternalToExternalMap,
            Map<String, String> amrExternalToInternalMap) {

        String uri = amrEntryElement.getAttributeValue(URI_ATTR_QNAME);
        String method = amrEntryElement.getAttributeValue(METHOD_ATTR_QNAME);
        Set<String> externalMappings = amrInternalToExternalMap.computeIfAbsent(method, k -> new HashSet<>());
        if (amrEntryElement.getAttribute(NIL_QNAME) == null) {
            externalMappings.add(uri);
        } else {
            externalMappings.add(String.valueOf(Character.MIN_VALUE));
        }
        amrExternalToInternalMap.put(uri, method);
    }

    private void processAcrEntry(OMElement amrEntryElement, Map<String, String> acrExternalToInternalMap,
            Map<String, Set<String>> acrInternalToExternalMap) {

        String uri = amrEntryElement.getAttributeValue(URI_ATTR_QNAME);
        String level = amrEntryElement.getAttributeValue(LEVEL_ATTR_QNAME);
        acrExternalToInternalMap.put(uri, level);
        Set<String> externalMappings = acrInternalToExternalMap.get(level);
        if (externalMappings == null) {
            externalMappings = new HashSet<>();
            acrInternalToExternalMap.put(level, externalMappings);
        }
        externalMappings.add(uri);
        acrExternalToInternalMap.put(uri, level);
    }

    @Override
    public String translateToInternalAmr(String uri, String protocol) {

        return amrExternalToInternalMap.get(uri);
    }

    @Override
    public Set<String> translateToExternalAmr(String method, String protocol) {

        Set<String> result = amrInternalToExternalMap.get(method);
        if (result == null) {
            return Collections.emptySet();
        }
        return result;
    }
}
