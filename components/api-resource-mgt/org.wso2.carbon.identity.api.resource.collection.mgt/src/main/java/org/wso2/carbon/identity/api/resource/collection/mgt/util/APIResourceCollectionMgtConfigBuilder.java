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

package org.wso2.carbon.identity.api.resource.collection.mgt.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants.APIResourceCollectionConfigBuilderConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionBasicInfo;
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
 * Config builder class for application resource collection configs in api-resource-collection.xml file.
 */
public class APIResourceCollectionMgtConfigBuilder {

    private static final Log LOG = LogFactory.getLog(APIResourceCollectionMgtConfigBuilder.class);
    private static final Map<String, APIResourceCollectionBasicInfo> apiResourceCollectionMgtConfigurations =
            new HashMap<>();
    private static final APIResourceCollectionMgtConfigBuilder apiResourceCollectionMgtConfigBuilder =
            new APIResourceCollectionMgtConfigBuilder();

    private OMElement documentElement;

    public static APIResourceCollectionMgtConfigBuilder getInstance() {

        return apiResourceCollectionMgtConfigBuilder;
    }

    private APIResourceCollectionMgtConfigBuilder() {

        loadConfiguration();
    }

    /**
     * Get the API resource collection mgt configurations.
     *
     * @return Map of API resource collection mgt configurations.
     */
    public Map<String, APIResourceCollectionBasicInfo> getApiResourceCollectionMgtConfigurations() {

        return apiResourceCollectionMgtConfigurations;
    }

    /**
     * Read the api-resource-collection.xml file and build the configuration map.
     */
    private void loadConfiguration() {

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
            buildAPIResourceCollectionConfig();
        } catch (IOException e) {
            LOG.warn("Error while loading system API resource management configs.", e);
        } catch (XMLStreamException e) {
            LOG.warn("Error while streaming system API resource management configs.", e);
        }
    }

    private void buildAPIResourceCollectionConfig() {

        Iterator<OMElement> apiResourceCollections = this.documentElement.getChildrenWithName(
                new QName(APIResourceCollectionConfigBuilderConstants.API_RESOURCE_COLLECTION_ELEMENT));

        while (apiResourceCollections.hasNext()) {
            OMElement apiResourceCollection = apiResourceCollections.next();
            APIResourceCollectionBasicInfo apiResourceCollectionObj =
                    buildAPIResourceCollectionBasicInfo(apiResourceCollection);
            if (apiResourceCollectionObj == null) {
                continue;
            }

            // Fetch scopes.
            OMElement scopesElement = apiResourceCollection.getFirstChildWithName(
                    new QName(APIResourceCollectionConfigBuilderConstants.SCOPES_ELEMENT));
            if (scopesElement != null) {
                Iterator<OMElement> scopes = scopesElement.getChildrenWithName(
                        new QName(APIResourceCollectionConfigBuilderConstants.SCOPE_ELEMENT));
                if (scopes != null) {
                    List<Scope> scopeList = new ArrayList<>();
                    while (scopes.hasNext()) {
                        OMElement scope = scopes.next();
                        Scope scopeObj = new Scope.ScopeBuilder()
                                .name(scope.getAttributeValue(
                                        new QName(APIResourceCollectionConfigBuilderConstants.NAME)))
                                .build();
                        scopeList.add(scopeObj);
                    }
                    apiResourceCollectionObj.setScopes(scopeList);
                }
            }
            apiResourceCollectionMgtConfigurations.put(apiResourceCollectionObj.getId(), apiResourceCollectionObj);
        }
    }

    private APIResourceCollectionBasicInfo buildAPIResourceCollectionBasicInfo(OMElement element) {

        String id = element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.ID));
        if (apiResourceCollectionMgtConfigurations.containsKey(id)) {
            return null;
        }

        return new APIResourceCollectionBasicInfo.APIResourceCollectionBasicInfoBuilder()
                .id(id)
                .name(element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.NAME)))
                .displayName(
                        element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.DISPLAY_NAME)))
                .type(element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.TYPE)))
                .build();
    }
}
