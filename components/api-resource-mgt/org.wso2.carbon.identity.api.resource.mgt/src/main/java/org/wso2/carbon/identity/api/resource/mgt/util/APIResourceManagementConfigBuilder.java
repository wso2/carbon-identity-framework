/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.api.resource.mgt.util;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.APIResourceConfigBuilderConstants;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Config builder class for system API resource configs in system-api-resource.xml file.
 */
public class APIResourceManagementConfigBuilder {

    private static final Log LOG = LogFactory.getLog(APIResourceManagementConfigBuilder.class);
    private static final Map<String, APIResource> apiResourceMgtConfigurations = new HashMap<>();
    private static final Map<String, APIResource> duplicateAPIResourceConfigs = new HashMap<>();
    private static final APIResourceManagementConfigBuilder apiResourceManagementConfigBuilder =
            new APIResourceManagementConfigBuilder();

    private OMElement documentElement;

    public static APIResourceManagementConfigBuilder getInstance() {

        return apiResourceManagementConfigBuilder;
    }

    private APIResourceManagementConfigBuilder() {

        loadConfigurations();
    }

    /**
     * Get system API resource configs.
     *
     * @return Map of API resource configs.
     */
    public Map<String, APIResource> getAPIResourceMgtConfigurations() {

        return apiResourceMgtConfigurations;
    }

    /**
     * Get duplicate system API resource configs.
     *
     * @return Map of duplicate API resource configs.
     */
    public Map<String, APIResource> getDuplicateAPIResourceConfigs() {

        return duplicateAPIResourceConfigs;
    }

    /**
     * Read the system-api-resource.xml file and build the configuration map.
     */
    @SuppressWarnings(value = "PATH_TRAVERSAL_IN", justification = "Don't use any user input file.")
    private void loadConfigurations() {

        String configDirPath = CarbonUtils.getCarbonConfigDirPath();
        File configFile = new File(configDirPath, FilenameUtils.getName("system-api-resource.xml"));
        if (!configFile.exists()) {
            return;
        }
        try (InputStream stream = Files.newInputStream(configFile.toPath())) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            // Prevents using external resources when parsing xml.
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            // Prevents using external document type definition when parsing xml.
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader parser = factory.createXMLStreamReader(stream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            documentElement = builder.getDocumentElement();
            buildAPIResourceConfig();
        } catch (IOException e) {
            LOG.warn("Error while loading system API resource management configs.", e);
        } catch (XMLStreamException e) {
            LOG.warn("Error while streaming system API resource management configs.", e);
        }
    }

    private void buildAPIResourceConfig() {

        Iterator<OMElement> apiResources = this.documentElement.getChildrenWithName(
                new QName(APIResourceConfigBuilderConstants.API_RESOURCE_ELEMENT));
        if (apiResources == null) {
            return;
        }

        while (apiResources.hasNext()) {
            OMElement apiResource = apiResources.next();
            APIResource apiResourceObj = buildAPIResource(apiResource);
            if (apiResourceObj == null) {
                continue;
            }

            OMElement scopeElement = apiResource.getFirstChildWithName(
                    new QName(APIResourceConfigBuilderConstants.SCOPES_ELEMENT));
            if (scopeElement != null) {
                Iterator<OMElement> scopes = scopeElement.getChildrenWithName(
                        new QName(APIResourceConfigBuilderConstants.SCOPE_ELEMENT));
                if (scopes != null) {
                    List<Scope> scopeList = new ArrayList<>();
                    while (scopes.hasNext()) {
                        OMElement scope = scopes.next();
                        Scope scopeObj = new Scope.ScopeBuilder()
                                .name(scope.getAttributeValue(new QName(APIResourceConfigBuilderConstants.NAME)))
                                .displayName(scope.getAttributeValue(
                                        new QName(APIResourceConfigBuilderConstants.DISPLAY_NAME)))
                                .description(scope.getAttributeValue(
                                        new QName(APIResourceConfigBuilderConstants.DESCRIPTION)))
                                .build();
                        scopeList.add(scopeObj);
                    }
                    apiResourceObj.setScopes(scopeList);
                }
            }
            /* If an API resource with the same identifier already exists in the config map, add the second one
            to the duplicate list. During API resource registration, diff will be applied as a patch to the existing
            API resource. API resource in the duplicate config map will be considered as the original API resource.
            */
            if (apiResourceMgtConfigurations.containsKey(apiResourceObj.getIdentifier())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("API resource with duplicate identifier: " + apiResourceObj.getIdentifier() + " found.");
                }
                duplicateAPIResourceConfigs.put(apiResourceObj.getIdentifier(), apiResourceObj);
                continue;
            }
            apiResourceMgtConfigurations.put(apiResourceObj.getIdentifier(), apiResourceObj);
        }
    }

    private APIResource buildAPIResource(OMElement element) {

        String apiResourceIdentifier = element.getAttributeValue(
                new QName(APIResourceConfigBuilderConstants.IDENTIFIER));
        String type = APIResourceConfigBuilderConstants.TENANT_ADMIN_TYPE;
        if (element.getAttributeValue(new QName(APIResourceConfigBuilderConstants.TYPE)) != null) {
            type = element.getAttributeValue(new QName(APIResourceConfigBuilderConstants.TYPE));
        }
        return new APIResource.APIResourceBuilder()
                .name(element.getAttributeValue(new QName(APIResourceConfigBuilderConstants.NAME)))
                .description(element.getAttributeValue(new QName(APIResourceConfigBuilderConstants.DESCRIPTION)))
                .identifier(apiResourceIdentifier)
                .type(type)
                .requiresAuthorization(Boolean.parseBoolean(
                        element.getAttributeValue(new QName(APIResourceConfigBuilderConstants.REQUIRES_AUTHORIZATION))))
                .build();
    }
}
