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
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the granular create/update/delete fields added to {@link APIResourceCollection}.
 */
public class APIResourceCollectionTest {

    @Test
    public void testBuilderPopulatesGranularScopeFields() {

        List<String> createScopes = Arrays.asList("create_scope_1", "create_scope_2");
        List<String> updateScopes = Collections.singletonList("update_scope_1");
        List<String> deleteScopes = Collections.singletonList("delete_scope_1");

        APIResourceCollection collection = new APIResourceCollection.APIResourceCollectionBuilder()
                .id("id")
                .name("name")
                .createScopes(createScopes)
                .updateScopes(updateScopes)
                .deleteScopes(deleteScopes)
                .createFeatureScope("console:feature_create")
                .updateFeatureScope("console:feature_update")
                .deleteFeatureScope("console:feature_delete")
                .build();

        Assert.assertEquals(collection.getCreateScopes(), createScopes);
        Assert.assertEquals(collection.getUpdateScopes(), updateScopes);
        Assert.assertEquals(collection.getDeleteScopes(), deleteScopes);
        Assert.assertEquals(collection.getCreateFeatureScope(), "console:feature_create");
        Assert.assertEquals(collection.getUpdateFeatureScope(), "console:feature_update");
        Assert.assertEquals(collection.getDeleteFeatureScope(), "console:feature_delete");
    }

    @Test
    public void testGranularScopeSettersRoundTrip() {

        APIResourceCollection collection = new APIResourceCollection();

        List<String> createScopes = Collections.singletonList("create_scope");
        List<String> updateScopes = Collections.singletonList("update_scope");
        List<String> deleteScopes = Collections.singletonList("delete_scope");

        collection.setCreateScopes(createScopes);
        collection.setUpdateScopes(updateScopes);
        collection.setDeleteScopes(deleteScopes);
        collection.setCreateFeatureScope("console:feature_create");
        collection.setUpdateFeatureScope("console:feature_update");
        collection.setDeleteFeatureScope("console:feature_delete");

        Assert.assertEquals(collection.getCreateScopes(), createScopes);
        Assert.assertEquals(collection.getUpdateScopes(), updateScopes);
        Assert.assertEquals(collection.getDeleteScopes(), deleteScopes);
        Assert.assertEquals(collection.getCreateFeatureScope(), "console:feature_create");
        Assert.assertEquals(collection.getUpdateFeatureScope(), "console:feature_update");
        Assert.assertEquals(collection.getDeleteFeatureScope(), "console:feature_delete");
    }

    @Test
    public void testGranularScopeFieldsDefaultToNull() {

        APIResourceCollection collection = new APIResourceCollection();

        Assert.assertNull(collection.getCreateScopes());
        Assert.assertNull(collection.getUpdateScopes());
        Assert.assertNull(collection.getDeleteScopes());
        Assert.assertNull(collection.getCreateFeatureScope());
        Assert.assertNull(collection.getUpdateFeatureScope());
        Assert.assertNull(collection.getDeleteFeatureScope());
    }
}
