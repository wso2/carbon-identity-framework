/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.model;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link AccessConfig}, covering path flattening, per-path encryption lookups
 * (including annotation stripping on modify paths) and the static area/leaf/read-only helpers.
 */
public class AccessConfigTest {

    @Test
    public void testNullListsYieldEmptyPathsAndNullEntries() {

        AccessConfig config = new AccessConfig(null, null);

        assertEquals(config.getExpose(), null);
        assertEquals(config.getModify(), null);
        assertTrue(config.getExposePaths().isEmpty());
        assertTrue(config.getModifyPaths().isEmpty());
        assertFalse(config.isExposePathEncrypted("/user/id"));
        assertFalse(config.isModifyPathEncrypted("/user/claims[uri=x]"));
    }

    @Test
    public void testExposeAndModifyPathsAreFlattened() {

        AccessConfig config = new AccessConfig(
                Arrays.asList(new ContextPath("/user/id", false),
                        new ContextPath("/user/username", true)),
                Collections.singletonList(new ContextPath("/user/credentials/password", true)));

        assertEquals(config.getExposePaths(), Arrays.asList("/user/id", "/user/username"));
        assertEquals(config.getModifyPaths(), Collections.singletonList("/user/credentials/password"));
        // The stored list is a defensive, unmodifiable copy.
        assertEquals(config.getExpose().size(), 2);
        assertEquals(config.getModify().size(), 1);
    }

    @Test
    public void testIsExposePathEncrypted() {

        AccessConfig config = new AccessConfig(
                Arrays.asList(new ContextPath("/user/id", false),
                        new ContextPath("/user/username", true)),
                null);

        assertTrue(config.isExposePathEncrypted("/user/username"));
        assertFalse(config.isExposePathEncrypted("/user/id"));
        assertFalse(config.isExposePathEncrypted("/user/unknown"));
    }

    @Test
    public void testIsModifyPathEncryptedStripsAnnotation() {

        AccessConfig config = new AccessConfig(null,
                Arrays.asList(new ContextPath("/user/claims[uri=http://wso2.org/claims/x]{[string]}", true),
                        new ContextPath("/user/credentials/token", false)));

        // The annotation ({[string]}) is stripped before matching the clean path.
        assertTrue(config.isModifyPathEncrypted("/user/claims[uri=http://wso2.org/claims/x]"));
        assertFalse(config.isModifyPathEncrypted("/user/credentials/token"));
        assertFalse(config.isModifyPathEncrypted("/user/credentials/unknown"));
    }

    @Test
    public void testIsReadOnly() {

        assertTrue(AccessConfig.isReadOnly("/flow/flowType"));
        assertFalse(AccessConfig.isReadOnly("/user/id"));
        assertFalse(AccessConfig.isReadOnly(null));
    }

    @Test
    public void testAnyExposedUnder() {

        List<String> leaves = Arrays.asList("/user/id", "/user/credentials/password");

        assertTrue(AccessConfig.anyExposedUnder("/user/credentials/", leaves));
        assertTrue(AccessConfig.anyExposedUnder("/user/", leaves));
        assertFalse(AccessConfig.anyExposedUnder("/organization/", leaves));
        assertFalse(AccessConfig.anyExposedUnder(null, leaves));
        assertFalse(AccessConfig.anyExposedUnder("/user/", null));
        assertFalse(AccessConfig.anyExposedUnder("/user/", Collections.emptyList()));
    }

    @Test
    public void testIsExposedPath() {

        List<String> leaves = Arrays.asList("/user/id", "/user/username");

        assertTrue(AccessConfig.isExposedPath("/user/id", leaves));
        assertFalse(AccessConfig.isExposedPath("/user/userStoreDomain", leaves));
        assertFalse(AccessConfig.isExposedPath(null, leaves));
        assertFalse(AccessConfig.isExposedPath("/user/id", null));
        assertFalse(AccessConfig.isExposedPath("/user/id", Collections.emptyList()));
    }
}
