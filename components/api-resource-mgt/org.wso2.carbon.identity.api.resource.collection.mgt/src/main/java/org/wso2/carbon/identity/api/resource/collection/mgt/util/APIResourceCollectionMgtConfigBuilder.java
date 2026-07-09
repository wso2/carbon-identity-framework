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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants.APIResourceCollectionConfigBuilderConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Config builder class for application resource collection configs in api-resource-collection.xml file.
 */
public class APIResourceCollectionMgtConfigBuilder {

    private static final Log LOG = LogFactory.getLog(APIResourceCollectionMgtConfigBuilder.class);
    private static final Map<String, APIResourceCollection> apiResourceCollectionMgtConfigurations = new HashMap<>();
    private static final APIResourceCollectionMgtConfigBuilder INSTANCE = new APIResourceCollectionMgtConfigBuilder();
    private OMElement documentElement;

    public static APIResourceCollectionMgtConfigBuilder getInstance() {

        return INSTANCE;
    }

    private APIResourceCollectionMgtConfigBuilder() {

        loadConfiguration();
    }

    /**
     * Get the API resource collection mgt configurations.
     *
     * @return Map of API resource collection mgt configurations.
     */
    public Map<String, APIResourceCollection> getApiResourceCollectionMgtConfigurations() {

        if (apiResourceCollectionMgtConfigurations.isEmpty()) {
            loadConfiguration();
        }
        return apiResourceCollectionMgtConfigurations;
    }

    /**
     * Read the api-resource-collection.xml file and build the configuration map.
     */
    private void loadConfiguration() {

        String configDirPath = CarbonUtils.getCarbonConfigDirPath();
        File configFile = new File(configDirPath,
                FilenameUtils.getName(APIResourceCollectionManagementConstants.API_RESOURCE_COLLECTION_FILE_NAME));
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
            LOG.error("Error while loading API resource collection management configs.", e);
        } catch (XMLStreamException e) {
            LOG.error("Error while streaming API resource collection management configs.", e);
        }
    }

    private void buildAPIResourceCollectionConfig() {

        Map<String, Set<String>> holderResolutionMap = buildHolderResolutionMap();

        Iterator<OMElement> apiResourceCollections = this.documentElement.getChildrenWithName(
                new QName(APIResourceCollectionConfigBuilderConstants.API_RESOURCE_COLLECTION_ELEMENT));

        while (apiResourceCollections.hasNext()) {
            OMElement apiResourceCollection = apiResourceCollections.next();
            APIResourceCollection apiResourceCollectionObj =
                    buildAPIResourceCollectionBasicInfo(apiResourceCollection);
            if (apiResourceCollectionObj == null) {
                continue;
            }
            String collectionVersion = apiResourceCollection
                    .getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.VERSION));
            OMElement scopesElement = apiResourceCollection.getFirstChildWithName(
                    new QName(APIResourceCollectionConfigBuilderConstants.SCOPES_ELEMENT));
            if (scopesElement != null) {
                Set<String> readScopeSet = new HashSet<>();
                Set<String> writeScopeSet = new HashSet<>();
                Set<String> createScopeSet = new HashSet<>();
                Set<String> updateScopeSet = new HashSet<>();
                Set<String> deleteScopeSet = new HashSet<>();
                Iterator<?> actionElements = scopesElement.getChildElements();
                while (actionElements.hasNext()) {
                    OMElement actionElement = (OMElement) actionElements.next();
                    if (actionElement == null) {
                        continue;
                    }
                    String actionName = actionElement.getLocalName();
                    Iterator<OMElement> scopes = actionElement.getChildrenWithName(
                            new QName(APIResourceCollectionConfigBuilderConstants.SCOPE_ELEMENT));
                    while (scopes.hasNext()) {
                        OMElement scope = scopes.next();
                        String scopeName = scope.getAttributeValue(
                                new QName(APIResourceCollectionConfigBuilderConstants.NAME));
                        // Read and old Feature scope are considered as read scopes.
                        boolean isReadAction = APIResourceCollectionConfigBuilderConstants.READ.equals(actionName);
                        boolean isCreateAction = APIResourceCollectionConfigBuilderConstants.CREATE.equals(actionName);
                        boolean isUpdateAction = APIResourceCollectionConfigBuilderConstants.UPDATE.equals(actionName);
                        boolean isDeleteAction = APIResourceCollectionConfigBuilderConstants.DELETE.equals(actionName);
                        boolean isFeatureAction = APIResourceCollectionConfigBuilderConstants.FEATURE
                            .equals(actionName);
                        if (APIResourceCollectionConfigBuilderConstants.COLLECTION_VERSION_V0
                                .equals(collectionVersion)) {
                            if (isReadAction || isFeatureAction) {
                                readScopeSet.add(scopeName);
                            } else {
                                // Create / Update / Delete actions aggregate into write and into their own bucket.
                                writeScopeSet.add(scopeName);
                                if (isCreateAction) {
                                    createScopeSet.add(scopeName);
                                } else if (isUpdateAction) {
                                    updateScopeSet.add(scopeName);
                                } else if (isDeleteAction) {
                                    deleteScopeSet.add(scopeName);
                                }
                            }
                        } else {
                            // Process new scopes with feature scopes.
                            if (isReadAction) {
                                if (isHolderScope(scopeName)) {
                                    readScopeSet.addAll(holderResolutionMap.getOrDefault(
                                            scopeName, Collections.emptySet()));
                                } else {
                                    readScopeSet.add(scopeName);
                                }
                            } else if (isCreateAction) {
                                if (isHolderScope(scopeName)) {
                                    Set<String> resolved = holderResolutionMap.getOrDefault(
                                            scopeName, Collections.emptySet());
                                    createScopeSet.addAll(resolved);
                                    writeScopeSet.addAll(resolved);
                                } else {
                                    createScopeSet.add(scopeName);
                                    writeScopeSet.add(scopeName);
                                }
                            } else if (isUpdateAction) {
                                if (isHolderScope(scopeName)) {
                                    Set<String> resolved = holderResolutionMap.getOrDefault(
                                            scopeName, Collections.emptySet());
                                    updateScopeSet.addAll(resolved);
                                    writeScopeSet.addAll(resolved);
                                } else {
                                    updateScopeSet.add(scopeName);
                                    writeScopeSet.add(scopeName);
                                }
                            } else if (isDeleteAction) {
                                if (isHolderScope(scopeName)) {
                                    Set<String> resolved = holderResolutionMap.getOrDefault(
                                            scopeName, Collections.emptySet());
                                    deleteScopeSet.addAll(resolved);
                                    writeScopeSet.addAll(resolved);
                                } else {
                                    deleteScopeSet.add(scopeName);
                                    writeScopeSet.add(scopeName);
                                }
                            } else if (isFeatureAction) {
                                if (isViewFeatureScope(scopeName)) {
                                    apiResourceCollectionObj.setViewFeatureScope(scopeName);
                                    readScopeSet.add(scopeName);
                                } else if (isEditFeatureScope(scopeName)) {
                                    apiResourceCollectionObj.setEditFeatureScope(scopeName);
                                    writeScopeSet.add(scopeName);
                                } else if (isCreateFeatureScope(scopeName)) {
                                    apiResourceCollectionObj.setCreateFeatureScope(scopeName);
                                    createScopeSet.add(scopeName);
                                } else if (isUpdateFeatureScope(scopeName)) {
                                    apiResourceCollectionObj.setUpdateFeatureScope(scopeName);
                                    updateScopeSet.add(scopeName);
                                } else if (isDeleteFeatureScope(scopeName)) {
                                    apiResourceCollectionObj.setDeleteFeatureScope(scopeName);
                                    deleteScopeSet.add(scopeName);
                                } else {
                                    readScopeSet.add(scopeName);
                                }
                            } else {
                                writeScopeSet.add(scopeName);
                            }
                        }
                    }
                }
                if (APIResourceCollectionConfigBuilderConstants.COLLECTION_VERSION_V0
                        .equals(collectionVersion)) {
                    apiResourceCollectionObj.setLegacyReadScopes(new ArrayList<>(readScopeSet));
                    apiResourceCollectionObj.setLegacyWriteScopes(new ArrayList<>(writeScopeSet));
                    if (apiResourceCollectionObj.getReadScopes() == null) {
                        apiResourceCollectionObj.setReadScopes(new ArrayList<>(readScopeSet));
                    }
                    if (apiResourceCollectionObj.getWriteScopes() == null) {
                        apiResourceCollectionObj.setWriteScopes(new ArrayList<>(writeScopeSet));
                    }
                    if (apiResourceCollectionObj.getCreateScopes() == null) {
                        apiResourceCollectionObj.setCreateScopes(new ArrayList<>(createScopeSet));
                    }
                    if (apiResourceCollectionObj.getUpdateScopes() == null) {
                        apiResourceCollectionObj.setUpdateScopes(new ArrayList<>(updateScopeSet));
                    }
                    if (apiResourceCollectionObj.getDeleteScopes() == null) {
                        apiResourceCollectionObj.setDeleteScopes(new ArrayList<>(deleteScopeSet));
                    }
                } else {
                    apiResourceCollectionObj.setReadScopes(new ArrayList<>(readScopeSet));
                    apiResourceCollectionObj.setWriteScopes(new ArrayList<>(writeScopeSet));
                    apiResourceCollectionObj.setCreateScopes(new ArrayList<>(createScopeSet));
                    apiResourceCollectionObj.setUpdateScopes(new ArrayList<>(updateScopeSet));
                    apiResourceCollectionObj.setDeleteScopes(new ArrayList<>(deleteScopeSet));
                }
            }
            apiResourceCollectionMgtConfigurations.put(apiResourceCollectionObj.getId(), apiResourceCollectionObj);
        }
    }

    private APIResourceCollection buildAPIResourceCollectionBasicInfo(OMElement element) {

        // Retrieve the name and encode it using base64, then remove "=" characters.
        String name = element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.NAME));
        String encodedName = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)).replace(
                APIResourceCollectionManagementConstants.EQUAL_SIGN, StringUtils.EMPTY);
        if (apiResourceCollectionMgtConfigurations.containsKey(encodedName)) {
            return apiResourceCollectionMgtConfigurations.get(encodedName);
        }

        return new APIResourceCollection.APIResourceCollectionBuilder()
                .id(encodedName)
                .name(element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.NAME)))
                .displayName(
                        element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.DISPLAY_NAME)))
                .type(element.getAttributeValue(new QName(APIResourceCollectionConfigBuilderConstants.TYPE)))
                .build();
    }

    private boolean isViewFeatureScope(String scope) {

        return StringUtils.isNotBlank(scope) &&
                scope.startsWith(APIResourceCollectionConfigBuilderConstants.CONSOLE_SCOPE_PREFIX) &&
                scope.endsWith(APIResourceCollectionConfigBuilderConstants.VIEW_FEATURE_SCOPE_SUFFIX);
    }

    private boolean isEditFeatureScope(String scope) {

        return StringUtils.isNotBlank(scope) &&
                scope.startsWith(APIResourceCollectionConfigBuilderConstants.CONSOLE_SCOPE_PREFIX) &&
                scope.endsWith(APIResourceCollectionConfigBuilderConstants.EDIT_FEATURE_SCOPE_SUFFIX);
    }

    private boolean isCreateFeatureScope(String scope) {

        return StringUtils.isNotBlank(scope) &&
                scope.startsWith(APIResourceCollectionConfigBuilderConstants.CONSOLE_SCOPE_PREFIX) &&
                scope.endsWith(APIResourceCollectionConfigBuilderConstants.CREATE_FEATURE_SCOPE_SUFFIX);
    }

    private boolean isUpdateFeatureScope(String scope) {

        return StringUtils.isNotBlank(scope) &&
                scope.startsWith(APIResourceCollectionConfigBuilderConstants.CONSOLE_SCOPE_PREFIX) &&
                scope.endsWith(APIResourceCollectionConfigBuilderConstants.UPDATE_FEATURE_SCOPE_SUFFIX);
    }

    private boolean isDeleteFeatureScope(String scope) {

        return StringUtils.isNotBlank(scope) &&
                scope.startsWith(APIResourceCollectionConfigBuilderConstants.CONSOLE_SCOPE_PREFIX) &&
                scope.endsWith(APIResourceCollectionConfigBuilderConstants.DELETE_FEATURE_SCOPE_SUFFIX);
    }

    private boolean isHolderScope(String scope) {

        return isViewFeatureScope(scope) || isEditFeatureScope(scope) || isCreateFeatureScope(scope)
                || isUpdateFeatureScope(scope) || isDeleteFeatureScope(scope);
    }

    /**
     * Build a map of holder scopes to their raw leaf scopes. Any nested holders are left unresolved.
     * Cycles are short-circuited to an empty branch.
     */
    private Map<String, Set<String>> buildHolderResolutionMap() {

        Map<String, Set<String>> rawMap = new HashMap<>();
        Iterator<OMElement> collections = this.documentElement.getChildrenWithName(
                new QName(APIResourceCollectionConfigBuilderConstants.API_RESOURCE_COLLECTION_ELEMENT));
        while (collections.hasNext()) {
            OMElement collection = collections.next();
            String version = collection.getAttributeValue(
                    new QName(APIResourceCollectionConfigBuilderConstants.VERSION));
            if (APIResourceCollectionConfigBuilderConstants.COLLECTION_VERSION_V0.equals(version)) {
                continue;
            }
            OMElement scopesElement = collection.getFirstChildWithName(
                    new QName(APIResourceCollectionConfigBuilderConstants.SCOPES_ELEMENT));
            if (scopesElement == null) {
                continue;
            }
            Map<String, Set<String>> scopesByBlock = collectScopesByBlock(scopesElement);
            Set<String> featureScopes = scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.FEATURE, Collections.emptySet());
            for (String featureScope : featureScopes) {
                Set<String> raw = resolveOwnerActionScopes(featureScope, scopesByBlock);
                if (raw != null) {
                    rawMap.put(featureScope, raw);
                }
            }
        }

        Map<String, Set<String>> resolvedMap = new HashMap<>();
        for (String holder : rawMap.keySet()) {
            resolveLeaves(holder, rawMap, resolvedMap, new HashSet<>());
        }
        return resolvedMap;
    }

    /**
     * Recursively flatten a holder's raw scope set into its leaf scopes. Any nested holder is replaced by
     * its own resolved leaves; cycles short-circuit to an empty branch. Memoized in {@code memo}.
     */
    private Set<String> resolveLeaves(String holder, Map<String, Set<String>> rawMap,
                                      Map<String, Set<String>> memo, Set<String> visiting) {

        if (memo.containsKey(holder)) {
            return memo.get(holder);
        }
        if (!visiting.add(holder)) {
            return Collections.emptySet();
        }
        Set<String> leaves = new HashSet<>();
        for (String scope : rawMap.getOrDefault(holder, Collections.emptySet())) {
            if (isHolderScope(scope)) {
                leaves.addAll(resolveLeaves(scope, rawMap, memo, visiting));
            } else {
                leaves.add(scope);
            }
        }
        visiting.remove(holder);
        memo.put(holder, leaves);
        return leaves;
    }

    private Map<String, Set<String>> collectScopesByBlock(OMElement scopesElement) {

        Map<String, Set<String>> scopesByBlock = new HashMap<>();
        Iterator<?> actionElements = scopesElement.getChildElements();
        while (actionElements.hasNext()) {
            OMElement actionElement = (OMElement) actionElements.next();
            if (actionElement == null) {
                continue;
            }
            String blockName = actionElement.getLocalName();
            Set<String> blockScopes = scopesByBlock.computeIfAbsent(blockName, k -> new HashSet<>());
            Iterator<OMElement> scopes = actionElement.getChildrenWithName(
                    new QName(APIResourceCollectionConfigBuilderConstants.SCOPE_ELEMENT));
            while (scopes.hasNext()) {
                String name = scopes.next().getAttributeValue(
                        new QName(APIResourceCollectionConfigBuilderConstants.NAME));
                if (StringUtils.isNotBlank(name)) {
                    blockScopes.add(name);
                }
            }
        }
        return scopesByBlock;
    }

    private Set<String> resolveOwnerActionScopes(String featureScope, Map<String, Set<String>> scopesByBlock) {

        if (isViewFeatureScope(featureScope)) {
            return new HashSet<>(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.READ, Collections.emptySet()));
        } else if (isCreateFeatureScope(featureScope)) {
            return new HashSet<>(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.CREATE, Collections.emptySet()));
        } else if (isUpdateFeatureScope(featureScope)) {
            return new HashSet<>(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.UPDATE, Collections.emptySet()));
        } else if (isDeleteFeatureScope(featureScope)) {
            return new HashSet<>(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.DELETE, Collections.emptySet()));
        } else if (isEditFeatureScope(featureScope)) {
            // Legacy coarse write — union of Create + Update + Delete leaf scopes.
            Set<String> union = new HashSet<>();
            union.addAll(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.CREATE, Collections.emptySet()));
            union.addAll(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.UPDATE, Collections.emptySet()));
            union.addAll(scopesByBlock.getOrDefault(
                    APIResourceCollectionConfigBuilderConstants.DELETE, Collections.emptySet()));
            return union;
        }
        return null;
    }
}
