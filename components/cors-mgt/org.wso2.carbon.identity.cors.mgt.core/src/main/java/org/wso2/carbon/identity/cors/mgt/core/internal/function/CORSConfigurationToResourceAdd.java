/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.function;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.ALLOW_ANY_ORIGIN;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.ALLOW_GENERIC_HTTP_REQUESTS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.ALLOW_SUBDOMAINS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.EXPOSED_HEADERS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.MAX_AGE;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.SUPPORTED_HEADERS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.SUPPORTED_METHODS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.SUPPORTS_CREDENTIALS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.SUPPORT_ANY_HEADER;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORSConfigurationAttributes.TAG_REQUESTS;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_CONFIGURATION_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.SerializationUtils.serializeStringSet;

/**
 * Converts a CORSConfiguration object to a ConfigurationManagement Resource.
 */
public class CORSConfigurationToResourceAdd implements Function<CORSConfiguration, ResourceAdd> {

    @Override
    public ResourceAdd apply(CORSConfiguration corsConfiguration) {

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(CORS_CONFIGURATION_RESOURCE_NAME);

        List<Attribute> attributes = new ArrayList<>();
        addAttribute(attributes, ALLOW_GENERIC_HTTP_REQUESTS,
                String.valueOf(corsConfiguration.isAllowGenericHttpRequests()));
        addAttribute(attributes, ALLOW_ANY_ORIGIN, String.valueOf(corsConfiguration.isAllowAnyOrigin()));
        addAttribute(attributes, ALLOW_SUBDOMAINS, String.valueOf(corsConfiguration.isAllowSubdomains()));
        addAttribute(attributes, SUPPORTED_METHODS, serializeStringSet(corsConfiguration.getSupportedMethods()));
        addAttribute(attributes, SUPPORT_ANY_HEADER, String.valueOf(corsConfiguration.isSupportAnyHeader()));
        addAttribute(attributes, SUPPORTED_HEADERS, serializeStringSet(corsConfiguration.getSupportedHeaders()));
        addAttribute(attributes, EXPOSED_HEADERS, serializeStringSet(corsConfiguration.getExposedHeaders()));
        addAttribute(attributes, SUPPORTS_CREDENTIALS, String.valueOf(corsConfiguration.isSupportsCredentials()));
        addAttribute(attributes, MAX_AGE, String.valueOf(corsConfiguration.getMaxAge()));
        addAttribute(attributes, TAG_REQUESTS, String.valueOf(corsConfiguration.isTagRequests()));

        resourceAdd.setAttributes(attributes);

        return resourceAdd;
    }

    private void addAttribute(List<Attribute> attributeList, String key, String value) {

        if (StringUtils.isNotBlank(value)) {
            Attribute attribute = new Attribute();
            attribute.setKey(key);
            attribute.setValue(value);
            attributeList.add(attribute);
        }
    }
}
