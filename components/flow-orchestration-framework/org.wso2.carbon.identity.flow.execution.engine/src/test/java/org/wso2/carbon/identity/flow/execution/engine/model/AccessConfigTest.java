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

package org.wso2.carbon.identity.flow.execution.engine.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link AccessConfig} and {@link ContextPath}.
 */
public class AccessConfigTest {

    @Test
    public void testAccessConfigWithNullLists() {

        AccessConfig config = new AccessConfig(null, null);
        assertNull(config.getExpose());
        assertNull(config.getModify());
        assertNull(config.getExposePaths());
        assertNull(config.getModifyPaths());
    }

    @Test
    public void testAccessConfigWithEmptyLists() {

        AccessConfig config = new AccessConfig(Collections.emptyList(), Collections.emptyList());
        assertNotNull(config.getExpose());
        assertTrue(config.getExpose().isEmpty());
        assertNotNull(config.getModify());
        assertTrue(config.getModify().isEmpty());
        assertNotNull(config.getExposePaths());
        assertTrue(config.getExposePaths().isEmpty());
        assertNotNull(config.getModifyPaths());
        assertTrue(config.getModifyPaths().isEmpty());
    }

    @Test
    public void testGetExposePaths() {

        List<ContextPath> exposePaths = Arrays.asList(
                new ContextPath("/user/claims/", true),
                new ContextPath("/user/credentials/", false)
        );
        AccessConfig config = new AccessConfig(exposePaths, null);

        List<String> paths = config.getExposePaths();
        assertEquals(paths.size(), 2);
        assertEquals(paths.get(0), "/user/claims/");
        assertEquals(paths.get(1), "/user/credentials/");
    }

    @Test
    public void testGetModifyPaths() {

        List<ContextPath> modifyPaths = Arrays.asList(
                new ContextPath("/properties/riskScore", false),
                new ContextPath("/user/claims/", true)
        );
        AccessConfig config = new AccessConfig(null, modifyPaths);

        List<String> paths = config.getModifyPaths();
        assertEquals(paths.size(), 2);
        assertEquals(paths.get(0), "/properties/riskScore");
        assertEquals(paths.get(1), "/user/claims/");
    }

    @Test
    public void testIsExposePathEncryptedMatchesLongestPrefix() {

        List<ContextPath> exposePaths = Arrays.asList(
                new ContextPath("/user/", false),
                new ContextPath("/user/claims/", true)
        );
        AccessConfig config = new AccessConfig(exposePaths, null);

        assertTrue(config.isExposePathEncrypted("/user/claims/email"));
        assertFalse(config.isExposePathEncrypted("/user/username"));
    }

    @Test
    public void testIsExposePathEncryptedNoMatch() {

        List<ContextPath> exposePaths = Collections.singletonList(
                new ContextPath("/user/claims/", true)
        );
        AccessConfig config = new AccessConfig(exposePaths, null);

        assertFalse(config.isExposePathEncrypted("/properties/riskScore"));
    }

    @Test
    public void testIsExposePathEncryptedWithNullExpose() {

        AccessConfig config = new AccessConfig(null, null);
        assertFalse(config.isExposePathEncrypted("/user/claims/email"));
    }

    @Test
    public void testIsModifyPathEncryptedMatchesLongestPrefix() {

        List<ContextPath> modifyPaths = Arrays.asList(
                new ContextPath("/user/", false),
                new ContextPath("/user/credentials/", true)
        );
        AccessConfig config = new AccessConfig(null, modifyPaths);

        assertTrue(config.isModifyPathEncrypted("/user/credentials/password"));
        assertFalse(config.isModifyPathEncrypted("/user/username"));
    }

    @Test
    public void testIsModifyPathEncryptedWithNullModify() {

        AccessConfig config = new AccessConfig(null, null);
        assertFalse(config.isModifyPathEncrypted("/user/credentials/password"));
    }

    @Test
    public void testIsModifyPathEncryptedWithAnnotatedPaths() {

        List<ContextPath> modifyPaths = Arrays.asList(
                new ContextPath("/properties/risk{risk: Float, factor: String}", true),
                new ContextPath("/properties/tags{[String]}", false)
        );
        AccessConfig config = new AccessConfig(null, modifyPaths);

        // Clean operation path should match annotated modify path after stripping.
        assertTrue(config.isModifyPathEncrypted("/properties/risk"));
        assertFalse(config.isModifyPathEncrypted("/properties/tags"));
        assertFalse(config.isModifyPathEncrypted("/properties/unknown"));
    }

    @Test
    public void testContextPathGetters() {

        ContextPath path = new ContextPath("/user/claims/email", true);
        assertEquals(path.getPath(), "/user/claims/email");
        assertTrue(path.isEncrypted());

        ContextPath unencrypted = new ContextPath("/properties/", false);
        assertEquals(unencrypted.getPath(), "/properties/");
        assertFalse(unencrypted.isEncrypted());
    }

    @Test
    public void testExposeListIsUnmodifiable() {

        List<ContextPath> exposePaths = Arrays.asList(
                new ContextPath("/user/claims/", true)
        );
        AccessConfig config = new AccessConfig(exposePaths, null);

        try {
            config.getExpose().add(new ContextPath("/hack/", false));
            // If no exception thrown, fail the test
            assertTrue(false, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testModifyListIsUnmodifiable() {

        List<ContextPath> modifyPaths = Arrays.asList(
                new ContextPath("/user/claims/", true)
        );
        AccessConfig config = new AccessConfig(null, modifyPaths);

        try {
            config.getModify().add(new ContextPath("/hack/", false));
            assertTrue(false, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
