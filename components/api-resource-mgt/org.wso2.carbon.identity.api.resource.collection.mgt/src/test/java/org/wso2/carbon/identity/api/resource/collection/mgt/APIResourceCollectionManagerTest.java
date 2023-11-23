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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants;
import org.wso2.carbon.identity.api.resource.collection.mgt.internal.APIResourceCollectionMgtServiceDataHolder;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.APIResourceCollectionMgtConfigBuilder;
import org.wso2.carbon.identity.api.resource.collection.mgt.util.FilterUtil;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@WithCarbonHome
@WithAxisConfiguration
@PrepareForTest({APIResourceCollectionMgtConfigBuilder.class, APIResourceCollectionManagerImpl.class, FilterUtil.class,
        APIResourceCollectionMgtServiceDataHolder.class, APIResourceManager.class, APIResourceManagerImpl.class})
public class APIResourceCollectionManagerTest extends PowerMockTestCase {

    private APIResourceCollectionManager apiResourceCollectionManager;
    private Map<String, APIResourceCollection> apiResourceCollectionMapMock;

    @BeforeMethod
    public void setUp() throws APIResourceMgtException {

        mockStatic(APIResourceCollectionMgtServiceDataHolder.class);
        APIResourceCollectionMgtServiceDataHolder serviceDataHolderMock =
                mock(APIResourceCollectionMgtServiceDataHolder.class);
        when(APIResourceCollectionMgtServiceDataHolder.getInstance()).thenReturn(serviceDataHolderMock);

        APIResourceManager apiResourceManagerMock = mock(APIResourceManager.class);
        when(serviceDataHolderMock.getAPIResourceManagementService()).thenReturn(apiResourceManagerMock);
        when(apiResourceManagerMock.getScopeMetadata(anyList(), anyString())).thenReturn(
                getListOfAPIResources());
        APIResourceCollectionMgtServiceDataHolder.getInstance().setAPIResourceManagementService(
                APIResourceManagerImpl.getInstance());

        apiResourceCollectionMapMock = new HashMap<>();
        addTestAPIResourceCollections();
        Whitebox.setInternalState(APIResourceCollectionManagerImpl.class, "apiResourceCollectionMap",
                apiResourceCollectionMapMock);
        apiResourceCollectionManager = APIResourceCollectionManagerImpl.getInstance();
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
}
