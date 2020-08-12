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

import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.SerializationUtils.deserializeStringSet;

/**
 * Converts a ConfigurationManagement Resource to a CORSConfiguration object.
 */
public class ResourceToCORSConfiguration implements Function<Resource, CORSConfiguration> {

    @Override
    public CORSConfiguration apply(Resource resource) {

        CORSConfiguration corsConfiguration = new CORSConfiguration();
        if (resource.isHasAttribute()) {
            List<Attribute> attributes = resource.getAttributes();
            Map<String, String> attributeMap = getAttributeMap(attributes);

            corsConfiguration.setAllowGenericHttpRequests(
                    Boolean.parseBoolean(attributeMap.get(ALLOW_GENERIC_HTTP_REQUESTS)));
            corsConfiguration.setAllowAnyOrigin(Boolean.parseBoolean(attributeMap.get(ALLOW_ANY_ORIGIN)));
            corsConfiguration.setAllowSubdomains(Boolean.parseBoolean(attributeMap.get(ALLOW_SUBDOMAINS)));
            corsConfiguration.setSupportedMethods(deserializeStringSet(attributeMap.get(SUPPORTED_METHODS)));
            corsConfiguration.setSupportAnyHeader(Boolean.parseBoolean(attributeMap.get(SUPPORT_ANY_HEADER)));
            corsConfiguration.setSupportedHeaders(deserializeStringSet(attributeMap.get(SUPPORTED_HEADERS)));
            corsConfiguration.setExposedHeaders(deserializeStringSet(attributeMap.get(EXPOSED_HEADERS)));
            corsConfiguration.setSupportsCredentials(Boolean.parseBoolean(attributeMap.get(SUPPORTS_CREDENTIALS)));
            corsConfiguration.setMaxAge(Integer.parseInt(attributeMap.get(MAX_AGE)));
            corsConfiguration.setTagRequests(Boolean.parseBoolean(attributeMap.get(TAG_REQUESTS)));
        }

        return corsConfiguration;
    }

    private Map<String, String> getAttributeMap(List<Attribute> attributes) {

        if (attributes != null) {
            return attributes.stream().collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
        }

        return Collections.emptyMap();
    }
}
