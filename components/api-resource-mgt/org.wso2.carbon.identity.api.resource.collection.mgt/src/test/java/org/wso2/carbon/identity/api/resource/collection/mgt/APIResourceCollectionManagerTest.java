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

package org.wso2.carbon.identity.api.resource.collection.mgt;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.internal.APIResourceCollectionMgtServiceDataHolder;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionManagementUtil;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithCarbonHome
@WithAxisConfiguration
public class APIResourceCollectionManagerTest {

    private APIResourceCollectionManager apiResourceCollectionManager;
    private Map<String, APIResourceCollection> apiResourceCollectionMapMock;
    private MockedStatic<APIResourceCollectionMgtServiceDataHolder> apiResourceCollectionMgtServiceData;
    private MockedStatic<APIResourceCollectionManagementUtil> apiResourceCollectionMgtUtil;
    private APIResourceManager apiResourceManagerMock;

    @BeforeMethod
    public void setUp() throws Exception {

        // Exercise the granular console permission path by default; the granular-disabled case re-stubs this to false.
        apiResourceCollectionMgtUtil = mockStatic(APIResourceCollectionManagementUtil.class, CALLS_REAL_METHODS);
        apiResourceCollectionMgtUtil.when(
                APIResourceCollectionManagementUtil::isGranularConsolePermissionsEnabled).thenReturn(true);

        apiResourceCollectionMgtServiceData = mockStatic(APIResourceCollectionMgtServiceDataHolder.class);
        APIResourceCollectionMgtServiceDataHolder serviceDataHolderMock =
                mock(APIResourceCollectionMgtServiceDataHolder.class);
        apiResourceCollectionMgtServiceData.when(
                APIResourceCollectionMgtServiceDataHolder::getInstance).thenReturn(serviceDataHolderMock);

        apiResourceManagerMock = mock(APIResourceManager.class);
        when(serviceDataHolderMock.getAPIResourceManagementService()).thenReturn(apiResourceManagerMock);
        when(apiResourceManagerMock.getScopeMetadata(anyList(), anyString())).thenReturn(
                getListOfAPIResources());

        apiResourceCollectionMapMock = new HashMap<>();
        addTestAPIResourceCollections();
        setPrivateStaticField(APIResourceCollectionManagerImpl.class, "apiResourceCollectionMap",
                apiResourceCollectionMapMock);
        apiResourceCollectionManager = APIResourceCollectionManagerImpl.getInstance();
    }

    @AfterMethod
    public void tearDown() {

        apiResourceCollectionMgtServiceData.close();
        apiResourceCollectionMgtUtil.close();
    }

    @DataProvider(name = "getAPIResourceCollectionsDataProvider")
    public Object[][] getAPIResourceCollectionsDataProvider() {

        return new Object[][]{
                {null, 3},
                {"name co test1", 1},
                {"name ew test1 and displayName co test1", 1}
        };
    }

    @Test(dataProvider = "getAPIResourceCollectionsDataProvider")
    public void testGetAPIResourceCollections(String filter, int expected) throws Exception {

        APIResourceCollectionSearchResult apiResourceSearchResult =
                apiResourceCollectionManager.getAPIResourceCollections(filter, null, "test");
        Assert.assertNotNull(apiResourceSearchResult.getAPIResourceCollections());
        Assert.assertEquals(apiResourceSearchResult.getAPIResourceCollections().size(), expected);
    }

    @Test
    public void testGetAPIResourceCollectionsById()
            throws Exception {

        addTestAPIResourceCollections();
        String encodedName1 = getEncodedName("nametest1");
        APIResourceCollection apiResourceCollection =
                apiResourceCollectionManager.getAPIResourceCollectionById(encodedName1, "test");
        Assert.assertNotNull(apiResourceCollection);
    }

    @Test
    public void testGetAPIResourceCollectionByIdPopulatesGranularApiResources() throws Exception {

        // Build a collection whose create/update/delete scopes line up with distinct mocked API resources.
        String name = "granular";
        APIResourceCollection granularCollection = new APIResourceCollection.APIResourceCollectionBuilder()
                .id(getEncodedName(name))
                .name(name)
                .displayName("Display Name granular")
                .type("BUSINESS")
                .readScopes(new ArrayList<>())
                .writeScopes(new ArrayList<>())
                .createScopes(singletonScopeList("testScopeOne test1"))
                .updateScopes(singletonScopeList("testScopeTwo test2"))
                .deleteScopes(singletonScopeList("testScopeOne test3"))
                .build();
        apiResourceCollectionMapMock.put(granularCollection.getId(), granularCollection);

        APIResourceCollection result =
                apiResourceCollectionManager.getAPIResourceCollectionById(getEncodedName(name), "test");

        Assert.assertNotNull(result);
        Map<String, List<APIResource>> apiResources = result.getApiResources();
        Assert.assertNotNull(apiResources);
        Assert.assertTrue(apiResources.containsKey(APIResourceCollectionManagementConstants.CREATE));
        Assert.assertTrue(apiResources.containsKey(APIResourceCollectionManagementConstants.UPDATE));
        Assert.assertTrue(apiResources.containsKey(APIResourceCollectionManagementConstants.DELETE));

        assertSingleResourceWithScope(apiResources.get(APIResourceCollectionManagementConstants.CREATE),
                "testScopeOne test1");
        assertSingleResourceWithScope(apiResources.get(APIResourceCollectionManagementConstants.UPDATE),
                "testScopeTwo test2");
        assertSingleResourceWithScope(apiResources.get(APIResourceCollectionManagementConstants.DELETE),
                "testScopeOne test3");
    }

    @Test
    public void testGranularEnabledWithNoActionScopesYieldsEmptyBuckets() throws Exception {

        // Granular enabled, but the collection declares no create/update/delete scopes: the action buckets are
        // emitted as present-but-empty lists (never null).
        addTestAPIResourceCollections();
        String encodedName1 = getEncodedName("nametest1");
        APIResourceCollection result =
                apiResourceCollectionManager.getAPIResourceCollectionById(encodedName1, "test");

        Assert.assertNotNull(result);
        Map<String, List<APIResource>> apiResources = result.getApiResources();
        Assert.assertNotNull(apiResources.get(APIResourceCollectionManagementConstants.CREATE));
        Assert.assertTrue(apiResources.get(APIResourceCollectionManagementConstants.CREATE).isEmpty());
        Assert.assertNotNull(apiResources.get(APIResourceCollectionManagementConstants.UPDATE));
        Assert.assertTrue(apiResources.get(APIResourceCollectionManagementConstants.UPDATE).isEmpty());
        Assert.assertNotNull(apiResources.get(APIResourceCollectionManagementConstants.DELETE));
        Assert.assertTrue(apiResources.get(APIResourceCollectionManagementConstants.DELETE).isEmpty());
    }

    @Test
    public void testGranularDisabledYieldsOnlyReadWriteBuckets() throws Exception {

        // With granular console permissions disabled, only the legacy read/write buckets are emitted; the
        // create/update/delete keys are absent altogether.
        apiResourceCollectionMgtUtil.when(
                APIResourceCollectionManagementUtil::isGranularConsolePermissionsEnabled).thenReturn(false);

        addTestAPIResourceCollections();
        String encodedName1 = getEncodedName("nametest1");
        APIResourceCollection result =
                apiResourceCollectionManager.getAPIResourceCollectionById(encodedName1, "test");

        Assert.assertNotNull(result);
        Map<String, List<APIResource>> apiResources = result.getApiResources();
        Assert.assertTrue(apiResources.containsKey(APIResourceCollectionManagementConstants.READ));
        Assert.assertTrue(apiResources.containsKey(APIResourceCollectionManagementConstants.WRITE));
        Assert.assertFalse(apiResources.containsKey(APIResourceCollectionManagementConstants.CREATE));
        Assert.assertFalse(apiResources.containsKey(APIResourceCollectionManagementConstants.UPDATE));
        Assert.assertFalse(apiResources.containsKey(APIResourceCollectionManagementConstants.DELETE));
    }

    // Plain feature scope shared across every action bucket.
    private static final String FEATURE_SCOPE = "console:apiResources";
    // Per-action feature scopes (console:*).
    private static final String VIEW_FEATURE_SCOPE = "console:apiResources_view";
    private static final String EDIT_FEATURE_SCOPE = "console:apiResources_edit";
    private static final String CREATE_FEATURE_SCOPE = "console:apiResources_create";
    private static final String UPDATE_FEATURE_SCOPE = "console:apiResources_update";
    private static final String DELETE_FEATURE_SCOPE = "console:apiResources_delete";
    // Per-action normal (internal_*) scopes.
    private static final String READ_NORMAL_SCOPE = "internal_api_resource_view";
    private static final String CREATE_NORMAL_SCOPE = "internal_api_resource_create";
    private static final String UPDATE_NORMAL_SCOPE = "internal_api_resource_update";
    private static final String DELETE_NORMAL_SCOPE = "internal_api_resource_delete";

    /**
     * Verifies the bucket contract enforced by {@code populateAPIResourcesForCollection}. The read scopes are merged
     * into every other bucket, so each action bucket carries the read-side scopes (plain feature + view feature +
     * read normal) on top of its own action and feature scopes. Granular actions do not bleed into one another.
     * <ul>
     *   <li>view    = plain feature + view feature + read normal</li>
     *   <li>write   = read scopes + edit feature + write normal (create/update/delete normal)</li>
     *   <li>create  = read scopes + create feature + create normal</li>
     *   <li>update  = read scopes + update feature + update normal</li>
     *   <li>delete  = read scopes + delete feature + delete normal</li>
     * </ul>
     */
    @Test
    public void testPopulatedBucketsCarryFeatureAndActionScopes() throws Exception {

        // A single API resource that owns every scope; the manager trims it down per bucket.
        APIResource allScopesResource = apiResourceWithScopes("collectionResource",
                FEATURE_SCOPE, VIEW_FEATURE_SCOPE, EDIT_FEATURE_SCOPE, CREATE_FEATURE_SCOPE, UPDATE_FEATURE_SCOPE,
                DELETE_FEATURE_SCOPE, READ_NORMAL_SCOPE, CREATE_NORMAL_SCOPE, UPDATE_NORMAL_SCOPE, DELETE_NORMAL_SCOPE);
        List<APIResource> metadata = new ArrayList<>();
        metadata.add(allScopesResource);
        when(apiResourceManagerMock.getScopeMetadata(anyList(), anyString())).thenReturn(metadata);

        // Buckets composed as the config builder emits them: the plain feature scope is read-side only and the
        // manager merges the read scopes into every other bucket.
        String name = "featureCollection";
        APIResourceCollection collection = new APIResourceCollection.APIResourceCollectionBuilder()
                .id(getEncodedName(name))
                .name(name)
                .displayName("Display Name " + name)
                .type("tenant")
                .readScopes(Arrays.asList(READ_NORMAL_SCOPE, VIEW_FEATURE_SCOPE, FEATURE_SCOPE))
                .writeScopes(Arrays.asList(CREATE_NORMAL_SCOPE, UPDATE_NORMAL_SCOPE, DELETE_NORMAL_SCOPE,
                        EDIT_FEATURE_SCOPE, VIEW_FEATURE_SCOPE, FEATURE_SCOPE))
                .createScopes(Arrays.asList(CREATE_NORMAL_SCOPE, CREATE_FEATURE_SCOPE,
                        READ_NORMAL_SCOPE, VIEW_FEATURE_SCOPE, FEATURE_SCOPE))
                .updateScopes(Arrays.asList(UPDATE_NORMAL_SCOPE, UPDATE_FEATURE_SCOPE,
                        READ_NORMAL_SCOPE, VIEW_FEATURE_SCOPE, FEATURE_SCOPE))
                .deleteScopes(Arrays.asList(DELETE_NORMAL_SCOPE, DELETE_FEATURE_SCOPE,
                        READ_NORMAL_SCOPE, VIEW_FEATURE_SCOPE, FEATURE_SCOPE))
                .build();
        apiResourceCollectionMapMock.put(collection.getId(), collection);

        APIResourceCollection result =
                apiResourceCollectionManager.getAPIResourceCollectionById(getEncodedName(name), "test");

        Assert.assertNotNull(result);
        Map<String, List<APIResource>> apiResources = result.getApiResources();

        // view (read) = plain feature + view feature + read normal.
        assertBucketScopes(apiResources, APIResourceCollectionManagementConstants.READ,
                FEATURE_SCOPE, VIEW_FEATURE_SCOPE, READ_NORMAL_SCOPE);
        // create = read scopes (plain feature) + create feature + create normal; no other action scopes bleed in.
        assertBucketScopes(apiResources, APIResourceCollectionManagementConstants.CREATE,
                CREATE_FEATURE_SCOPE, CREATE_NORMAL_SCOPE,
                VIEW_FEATURE_SCOPE, READ_NORMAL_SCOPE, FEATURE_SCOPE);
        // update = read scopes (plain feature) + update feature + update normal; no other action scopes bleed in.
        assertBucketScopes(apiResources, APIResourceCollectionManagementConstants.UPDATE,
                FEATURE_SCOPE, UPDATE_FEATURE_SCOPE, UPDATE_NORMAL_SCOPE,
                VIEW_FEATURE_SCOPE, READ_NORMAL_SCOPE, FEATURE_SCOPE);
        // delete = read scopes (plain feature) + delete feature + delete normal; no other action scopes bleed in.
        assertBucketScopes(apiResources, APIResourceCollectionManagementConstants.DELETE,
                FEATURE_SCOPE, DELETE_FEATURE_SCOPE, DELETE_NORMAL_SCOPE,
                VIEW_FEATURE_SCOPE, READ_NORMAL_SCOPE, FEATURE_SCOPE);
        // write = read scopes (plain feature) + edit feature + write normal (create/update/delete normal).
        assertBucketScopes(apiResources, APIResourceCollectionManagementConstants.WRITE,
                CREATE_NORMAL_SCOPE, UPDATE_NORMAL_SCOPE, DELETE_NORMAL_SCOPE, READ_NORMAL_SCOPE,
                VIEW_FEATURE_SCOPE, EDIT_FEATURE_SCOPE,  FEATURE_SCOPE);
    }

    /**
     * Asserts that the given bucket exists and the union of scopes across its API resources matches exactly the
     * expected set. Exact equality is required so that scopes from one granular bucket (create/update/delete)
     * cannot silently leak into another.
     */
    private static void assertBucketScopes(Map<String, List<APIResource>> apiResources, String bucketKey,
                                           String... expectedScopes) {

        List<APIResource> bucket = apiResources.get(bucketKey);
        Assert.assertNotNull(bucket, "Bucket should be present: " + bucketKey);
        Set<String> actual = new HashSet<>();
        for (APIResource apiResource : bucket) {
            for (Scope scope : apiResource.getScopes()) {
                actual.add(scope.getName());
            }
        }
        Set<String> expected = new HashSet<>(Arrays.asList(expectedScopes));
        Assert.assertEquals(actual, expected,
                "Bucket '" + bucketKey + "' scope set must match exactly. Expected " + expected
                        + " but had " + actual);
    }

    private static APIResource apiResourceWithScopes(String name, String... scopeNames) {

        List<Scope> scopes = new ArrayList<>();
        for (String scopeName : scopeNames) {
            scopes.add(createScope(scopeName));
        }
        return new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + name)
                .identifier("testAPIResource identifier " + name)
                .description("testAPIResource description " + name)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes)
                .build();
    }

    private static List<String> singletonScopeList(String scope) {

        List<String> scopes = new ArrayList<>();
        scopes.add(scope);
        return scopes;
    }

    private static void assertSingleResourceWithScope(List<APIResource> apiResources, String expectedScope) {

        Assert.assertNotNull(apiResources);
        Assert.assertEquals(apiResources.size(), 1,
                "Exactly one API resource should match the granular scope: " + expectedScope);
        List<Scope> scopes = apiResources.get(0).getScopes();
        Assert.assertEquals(scopes.size(), 1, "Only the matching scope should be retained on the resource.");
        Assert.assertEquals(scopes.get(0).getName(), expectedScope);
    }

    private void addTestAPIResourceCollections() {

        APIResourceCollection apiResourceCollection1 = createAPIResourceCollection("test1");
        APIResourceCollection apiResourceCollection2 = createAPIResourceCollection("test2");
        APIResourceCollection apiResourceCollection3 = createAPIResourceCollection("test3");
        apiResourceCollectionMapMock.put(apiResourceCollection1.getId(), apiResourceCollection1);
        apiResourceCollectionMapMock.put(apiResourceCollection2.getId(), apiResourceCollection2);
        apiResourceCollectionMapMock.put(apiResourceCollection3.getId(), apiResourceCollection3);
    }

    private static APIResourceCollection createAPIResourceCollection(String postFix) {

        List<String> readScopes = new ArrayList<>();
        readScopes.add("testReadScopeOne " + postFix);
        readScopes.add("testReadScopeTwo " + postFix);

        List<String> writeScopes = new ArrayList<>();
        writeScopes.add("testWriteScopeOne " + postFix);
        writeScopes.add("testWriteScopeTwo " + postFix);

        String name = "name" + postFix;
        String encodedName = getEncodedName(name);

        return new APIResourceCollection.APIResourceCollectionBuilder()
                .id(encodedName)
                .name(name)
                .displayName("Display Name" + postFix)
                .type("BUSINESS")
                .readScopes(readScopes)
                .writeScopes(writeScopes)
                .build();
    }

    /**
     * Create scope with the given name.
     *
     * @param name Name of the scope.
     * @return Scope.
     */
    private static Scope createScope(String name) {

        Scope.ScopeBuilder scopeBuilder = new Scope.ScopeBuilder()
                .name(name)
                .displayName("displayName " + name)
                .description("description " + name);
        return scopeBuilder.build();
    }

    /**
     * Create API resource with the given postfix.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @return API resource.
     */
    private static APIResource createAPIResource(String postFix) {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(createScope("testScopeOne " + postFix));
        scopes.add(createScope("testScopeTwo " + postFix));

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postFix)
                .identifier("testAPIResource identifier " + postFix)
                .description("testAPIResource description " + postFix)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes);
        return apiResourceBuilder.build();
    }

    /**
     * Get list of API resources.
     *
     * @return List of API resources.
     */
    private static List<APIResource> getListOfAPIResources() {

            List<APIResource> apiResources = new ArrayList<>();
            apiResources.add(createAPIResource("test1"));
            apiResources.add(createAPIResource("test2"));
            apiResources.add(createAPIResource("test3"));
            return apiResources;
    }

    /**
     * Get Base64 encoded name.
     *
     * @param name name to be encoded
     * @return encoded name
     */
    private static String getEncodedName(String name) {

        return Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8)).replace(
                APIResourceCollectionManagementConstants.EQUAL_SIGN, StringUtils.EMPTY);
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
