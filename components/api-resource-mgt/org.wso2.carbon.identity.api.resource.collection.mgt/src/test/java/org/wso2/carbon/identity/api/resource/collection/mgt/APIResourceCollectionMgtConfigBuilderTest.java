/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.collection.mgt;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionMgtConfigBuilder;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Unit tests for {@link APIResourceCollectionMgtConfigBuilder}.
 * <p>
 * The builder parses {@code api-resource-collection.xml} from the test resources. That config declares two
 * {@code apiResources} collections that share the same name (a {@code version="v0"} collection and a new one);
 * because the name is base64-encoded into the id, both merge into a single parsed {@link APIResourceCollection}.
 * The tests below pin the scope buckets produced for that merged collection, grouped by the concern they cover.
 */
@WithCarbonHome
@WithAxisConfiguration
public class APIResourceCollectionMgtConfigBuilderTest {

    private static final String COLLECTION_NAME = "apiResources";
    private static final String COLLECTION_DISPLAY_NAME = "API Resources";
    private static final String COLLECTION_TYPE = "tenant";

    // Action scopes (internal_*) declared under <Read>/<Create>/<Update>/<Delete>.
    private static final String READ_SCOPE = "internal_api_resource_view";
    private static final String CREATE_SCOPE = "internal_api_resource_create";
    private static final String UPDATE_SCOPE = "internal_api_resource_update";
    private static final String DELETE_SCOPE = "internal_api_resource_delete";

    // Feature scopes (console:*) declared under <Feature>.
    private static final String FEATURE_SCOPE = "console:apiResources";
    private static final String VIEW_FEATURE_SCOPE = "console:apiResources_view";
    private static final String EDIT_FEATURE_SCOPE = "console:apiResources_edit";
    private static final String CREATE_FEATURE_SCOPE = "console:apiResources_create";
    private static final String UPDATE_FEATURE_SCOPE = "console:apiResources_update";
    private static final String DELETE_FEATURE_SCOPE = "console:apiResources_delete";

    private APIResourceCollectionMgtConfigBuilder configBuilder;

    @BeforeMethod
    public void setUp() throws URISyntaxException {

        URL confResource = getClass().getResource(
                "/repository/conf/" + APIResourceCollectionManagementConstants.API_RESOURCE_COLLECTION_FILE_NAME);
        Assert.assertNotNull(confResource, "Test api-resource-collection.xml must be on the classpath.");
        String confDir = new File(confResource.toURI()).getParent();
        System.setProperty("carbon.config.dir.path", confDir);

        configBuilder = APIResourceCollectionMgtConfigBuilder.getInstance();
    }

    @Test(groups = "configLoading", description = "The config file is parsed into a non-empty collection map.")
    public void testConfigurationsAreLoaded() {

        Map<String, APIResourceCollection> configurations = configBuilder.getApiResourceCollectionMgtConfigurations();
        Assert.assertNotNull(configurations);
        Assert.assertFalse(configurations.isEmpty());
    }

    @Test(groups = "configLoading",
            description = "Basic info (id, name, display name, type) is parsed for the merged collection.")
    public void testApiResourcesCollectionBasicInfo() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertNotNull(collection.getId(), "Collection id should be set.");
        Assert.assertEquals(collection.getName(), COLLECTION_NAME);
        Assert.assertEquals(collection.getDisplayName(), COLLECTION_DISPLAY_NAME);
        Assert.assertEquals(collection.getType(), COLLECTION_TYPE);
    }

    @Test(groups = "readScopes",
            description = "Read scopes contain the read action scope and the view feature scopes, "
                    + "without write-side scopes leaking in.")
    public void testReadScopesContainReadAndPlainFeatureScopes() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertTrue(collection.getReadScopes().contains(READ_SCOPE));
        Assert.assertTrue(collection.getReadScopes().contains(FEATURE_SCOPE));
        Assert.assertTrue(collection.getReadScopes().contains(VIEW_FEATURE_SCOPE));

        // Write-side scopes should not leak into the read bucket.
        Assert.assertFalse(collection.getReadScopes().contains(CREATE_SCOPE));
        Assert.assertFalse(collection.getReadScopes().contains(EDIT_FEATURE_SCOPE));
    }

    @Test(groups = "writeScopes",
            description = "Create/update/delete action scopes are aggregated into the combined write bucket.")
    public void testWriteScopesAggregateCreateUpdateDelete() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertTrue(collection.getWriteScopes().contains(CREATE_SCOPE));
        Assert.assertTrue(collection.getWriteScopes().contains(UPDATE_SCOPE));
        Assert.assertTrue(collection.getWriteScopes().contains(DELETE_SCOPE));
    }

    @Test(groups = "writeScopes",
            description = "The edit feature scope is aggregated into the write bucket only (not a granular bucket).")
    public void testEditFeatureScopeAggregatedIntoWriteScopes() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertTrue(collection.getWriteScopes().contains(EDIT_FEATURE_SCOPE));
        
        Assert.assertFalse(collection.getCreateScopes().contains(EDIT_FEATURE_SCOPE));
        Assert.assertFalse(collection.getUpdateScopes().contains(EDIT_FEATURE_SCOPE));
        Assert.assertFalse(collection.getDeleteScopes().contains(EDIT_FEATURE_SCOPE));
    }

    @Test(groups = "writeScopes",
            description = "Write bucket holds the edit feature scope and the write normal (create/update/delete "
                    + "action) scopes.")
    public void testWriteScopesContainEditFeatureScopes() {

        APIResourceCollection collection = getApiResourcesCollection();

        // write feature + write normal scopes.
        Assert.assertTrue(collection.getWriteScopes().contains(EDIT_FEATURE_SCOPE));
        Assert.assertTrue(collection.getWriteScopes().contains(CREATE_SCOPE));
        Assert.assertTrue(collection.getWriteScopes().contains(UPDATE_SCOPE));
        Assert.assertTrue(collection.getWriteScopes().contains(DELETE_SCOPE));

        // The plain and view feature scopes are read-side; they are not stored in the write bucket itself.
        Assert.assertFalse(collection.getWriteScopes().contains(FEATURE_SCOPE));
        Assert.assertFalse(collection.getWriteScopes().contains(VIEW_FEATURE_SCOPE));
    }

    @Test(groups = "granularActionScopes",
            description = "Create/update/delete action scopes land in their own bucket and do not bleed across.")
    public void testGranularActionScopesAreBucketed() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertNotNull(collection.getCreateScopes());
        Assert.assertNotNull(collection.getUpdateScopes());
        Assert.assertNotNull(collection.getDeleteScopes());
        Assert.assertTrue(collection.getCreateScopes().contains(CREATE_SCOPE),
                "Create action scope should be bucketed into createScopes.");
        Assert.assertTrue(collection.getUpdateScopes().contains(UPDATE_SCOPE),
                "Update action scope should be bucketed into updateScopes.");
        Assert.assertTrue(collection.getDeleteScopes().contains(DELETE_SCOPE),
                "Delete action scope should be bucketed into deleteScopes.");

        // Granular buckets must not bleed into each other.
        Assert.assertFalse(collection.getCreateScopes().contains(UPDATE_SCOPE));
        Assert.assertFalse(collection.getCreateScopes().contains(DELETE_SCOPE));
        Assert.assertFalse(collection.getUpdateScopes().contains(CREATE_SCOPE));
        Assert.assertFalse(collection.getUpdateScopes().contains(DELETE_SCOPE));
        Assert.assertFalse(collection.getDeleteScopes().contains(CREATE_SCOPE));
        Assert.assertFalse(collection.getDeleteScopes().contains(UPDATE_SCOPE));
    }

    @Test(groups = "granularActionScopes",
            description = "Create bucket holds the create action scope and the create feature scope, without the "
                    + "plain feature scope (read-side) or the update/delete action scopes leaking in.")
    public void testCreateScopesContainCreateActionAndFeatureScopes() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertTrue(collection.getCreateScopes().contains(CREATE_SCOPE));
        Assert.assertTrue(collection.getCreateScopes().contains(CREATE_FEATURE_SCOPE));

        // Other write-side action scopes should not leak into the create bucket.
        Assert.assertFalse(collection.getCreateScopes().contains(UPDATE_SCOPE));
        Assert.assertFalse(collection.getCreateScopes().contains(DELETE_SCOPE));
    }

    @Test(groups = "granularFeatureScopes",
            description = "Granular console feature scopes resolve to their dedicated fields and matching buckets.")
    public void testGranularFeatureScopesAreResolved() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertEquals(collection.getViewFeatureScope(), VIEW_FEATURE_SCOPE);
        Assert.assertEquals(collection.getEditFeatureScope(), EDIT_FEATURE_SCOPE);
        Assert.assertEquals(collection.getCreateFeatureScope(), CREATE_FEATURE_SCOPE);
        Assert.assertEquals(collection.getUpdateFeatureScope(), UPDATE_FEATURE_SCOPE);
        Assert.assertEquals(collection.getDeleteFeatureScope(), DELETE_FEATURE_SCOPE);

        // Each granular feature scope is also added to its respective action bucket.
        Assert.assertTrue(collection.getCreateScopes().contains(CREATE_FEATURE_SCOPE));
        Assert.assertTrue(collection.getUpdateScopes().contains(UPDATE_FEATURE_SCOPE));
        Assert.assertTrue(collection.getDeleteScopes().contains(DELETE_FEATURE_SCOPE));
    }

    @Test(groups = "legacyScopes",
            description = "Legacy read/write scopes are populated from the v0 collection.")
    public void testLegacyScopesPopulatedFromV0Collection() {

        APIResourceCollection collection = getApiResourcesCollection();

        Assert.assertNotNull(collection.getLegacyReadScopes());
        Assert.assertTrue(collection.getLegacyReadScopes().contains(READ_SCOPE));
        Assert.assertTrue(collection.getLegacyReadScopes().contains(FEATURE_SCOPE));

        Assert.assertNotNull(collection.getLegacyWriteScopes());
        Assert.assertTrue(collection.getLegacyWriteScopes().contains(CREATE_SCOPE));
        Assert.assertTrue(collection.getLegacyWriteScopes().contains(UPDATE_SCOPE));
        Assert.assertTrue(collection.getLegacyWriteScopes().contains(DELETE_SCOPE));
    }

    /**
     * Resolves the single merged {@code apiResources} collection from the parsed config.
     */
    private APIResourceCollection getApiResourcesCollection() {

        Map<String, APIResourceCollection> configurations = configBuilder.getApiResourceCollectionMgtConfigurations();
        APIResourceCollection collection = configurations.values().stream()
                .filter(c -> COLLECTION_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(collection, "apiResources collection should be present in the parsed config.");
        return collection;
    }
}
