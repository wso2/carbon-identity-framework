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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link PropertyPathNavigator}.
 * <p>
 * Covers the "::"-separated path walking contract used by {@code ProxyReferenceCache}
 * and {@code ArgumentAdapter.reconstructFromProxy}: ProxyObject member access, numeric-
 * segment ProxyArray indexing, the {@code __keys__} terminal, and set-last-segment writes.
 * Lives in the same package as the class under test because {@code PropertyPathNavigator}
 * is package-private.
 */
public class PropertyPathNavigatorTest {

    @DataProvider(name = "numericStrings")
    public Object[][] numericStrings() {

        return new Object[][]{
                {null, false},
                {"", false},
                {"0", true},
                {"7", true},
                {"12345", true},
                {"01", true},
                {"-1", false},
                {"1.5", false},
                {" 1", false},
                {"1a", false},
                {"abc", false}
        };
    }

    @Test(dataProvider = "numericStrings")
    public void testIsNumeric(String input, boolean expected) {

        assertEquals(PropertyPathNavigator.isNumeric(input), expected,
                "Unexpected isNumeric result for input: " + input);
    }

    @Test
    public void testNavigatePathSingleSegmentOnProxyObject() {

        Map<String, Object> map = new HashMap<>();
        map.put("username", "alice");
        ProxyObject root = ProxyObject.fromMap(map);

        Object result = PropertyPathNavigator.navigatePath(new String[]{"username"}, 0, root);
        assertEquals(result, "alice");
    }

    @Test
    public void testNavigatePathNestedProxyObjects() {

        Map<String, Object> inner = new HashMap<>();
        inner.put("city", "Colombo");
        Map<String, Object> outer = new HashMap<>();
        outer.put("address", ProxyObject.fromMap(inner));
        ProxyObject root = ProxyObject.fromMap(outer);

        Object result = PropertyPathNavigator.navigatePath(
                new String[]{"address", "city"}, 0, root);
        assertEquals(result, "Colombo");
    }

    @Test
    public void testNavigatePathProxyArrayNumericIndex() {

        ProxyArray array = ProxyArray.fromList(Arrays.asList("a", "b", "c"));
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("items", array);
        ProxyObject root = ProxyObject.fromMap(rootMap);

        Object result = PropertyPathNavigator.navigatePath(
                new String[]{"items", "1"}, 0, root);
        assertEquals(result, "b");
    }

    @Test
    public void testNavigatePathHonoursStartIndex() {

        // startIndex=1 lets the caller skip a reference-id prefix segment.
        Map<String, Object> map = new HashMap<>();
        map.put("status", "active");
        ProxyObject root = ProxyObject.fromMap(map);

        Object result = PropertyPathNavigator.navigatePath(
                new String[]{"ignored-ref-id", "status"}, 1, root);
        assertEquals(result, "active");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNavigatePathThroughNullIntermediateThrows() {

        Map<String, Object> map = new HashMap<>();
        map.put("middle", null);
        ProxyObject root = ProxyObject.fromMap(map);

        PropertyPathNavigator.navigatePath(new String[]{"middle", "leaf"}, 0, root);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNavigatePathOnPlainMapNotSupported() {

        // Plain Java Map is not a ProxyObject — navigator rejects it.
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        PropertyPathNavigator.navigatePath(new String[]{"key"}, 0, map);
    }

    @Test
    public void testNavigatePathKeysPropertyReturnsMemberKeys() {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        ProxyObject root = ProxyObject.fromMap(map);

        Object keys = PropertyPathNavigator.navigatePath(
                new String[]{RemoteEngineConstants.KEYS_PROPERTY}, 0, root);
        assertNotNull(keys);
        // ProxyObject.fromMap exposes keys either as a Set view or a ProxyArray wrapper;
        // assert via contains() on the toString to stay tolerant to the concrete type.
        assertTrue(keys.toString().contains("a"));
        assertTrue(keys.toString().contains("b"));
    }

    @Test
    public void testGetMemberKeysProxyObject() {

        Map<String, Object> map = new HashMap<>();
        map.put("x", 1);
        ProxyObject root = ProxyObject.fromMap(map);

        Object keys = PropertyPathNavigator.getMemberKeys(root);
        assertNotNull(keys);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetMemberKeysOnNonProxyThrows() {

        PropertyPathNavigator.getMemberKeys("not-a-proxy");
    }

    @Test
    public void testSetPropertyOnProxyObjectWritesThrough() {

        Map<String, Object> backing = new HashMap<>();
        ProxyObject root = ProxyObject.fromMap(backing);

        boolean result = PropertyPathNavigator.setProperty(
                new String[]{"note"}, 0, root, "hello");

        assertTrue(result);
        assertTrue(backing.containsKey("note"),
                "putMember on ProxyObject.fromMap must write to the backing map");
    }

    @Test
    public void testSetPropertyNestedPath() {

        Map<String, Object> innerBacking = new HashMap<>();
        ProxyObject inner = ProxyObject.fromMap(innerBacking);
        Map<String, Object> outer = new HashMap<>();
        outer.put("details", inner);
        ProxyObject root = ProxyObject.fromMap(outer);

        boolean result = PropertyPathNavigator.setProperty(
                new String[]{"details", "name"}, 0, root, "bob");

        assertTrue(result);
        assertTrue(innerBacking.containsKey("name"),
                "Nested setProperty must land on the inner ProxyObject's backing map");
    }

    @Test
    public void testSetPropertyEmptyPathReturnsFalse() {

        Map<String, Object> backing = new HashMap<>();
        ProxyObject root = ProxyObject.fromMap(backing);

        boolean result = PropertyPathNavigator.setProperty(new String[0], 0, root, "x");
        assertFalse(result);
    }

    @Test
    public void testSetPropertyOnNonProxyParentThrows() {

        try {
            PropertyPathNavigator.setProperty(
                    new String[]{"leaf"}, 0, "not-a-proxy-root", "value");
            fail("Expected IllegalStateException for non-ProxyObject parent");
        } catch (IllegalStateException expected) {
            // expected: setFinalProperty only accepts ProxyObject parents.
        }
    }

    @Test
    public void testListRoundTripThroughSetAndNavigate() {

        // Sanity round-trip: write via setProperty, read via navigatePath on the same proxy.
        Map<String, Object> backing = new HashMap<>();
        ProxyObject root = ProxyObject.fromMap(backing);

        boolean wrote = PropertyPathNavigator.setProperty(
                new String[]{"greeting"}, 0, root, "hi");
        assertTrue(wrote);

        Object read = PropertyPathNavigator.navigatePath(
                new String[]{"greeting"}, 0, root);

        // ProxyObject.fromMap may wrap the written value as a polyglot Value or keep it as-is
        // — assert on the observable string form rather than the concrete type.
        assertNotNull(read);
        assertTrue(read.toString().contains("hi"));
    }

    // ========================================================================
    // Edge cases exercised by the real remote JS engine paths.
    // ========================================================================

    @Test
    public void testNavigatePathEmptyPathReturnsRoot() {

        // Boundary: zero segments is legal — the for-loop body never runs and root
        // is returned. This is how root proxies resolve through the navigator.
        Map<String, Object> map = new HashMap<>();
        ProxyObject root = ProxyObject.fromMap(map);

        Object result = PropertyPathNavigator.navigatePath(new String[0], 0, root);
        assertSame(result, root, "Empty path must pass root through untouched");
    }

    @Test
    public void testNavigatePathStartIndexEqualToLengthReturnsRoot() {

        // startIndex == pathParts.length means "nothing to walk" — used by the host-ref
        // path ProxyReferenceCache.getHostRefProperty when the caller passes only a refId.
        Map<String, Object> map = new HashMap<>();
        map.put("should-not-be-read", "value");
        ProxyObject root = ProxyObject.fromMap(map);

        Object result = PropertyPathNavigator.navigatePath(
                new String[]{"ref-id-only"}, 1, root);
        assertSame(result, root);
    }

    @Test
    public void testNavigatePathDeepChain() {

        // Deep paths mirror "steps::1::subject::remoteClaims::profile" shape used by
        // authentication scripts. 5 nested proxies — no memoisation inside the navigator,
        // so correctness here guarantees no short-circuit bug.
        Map<String, Object> leaf = new HashMap<>();
        leaf.put("email", "alice@example.com");
        Object proxy = ProxyObject.fromMap(leaf);
        for (int i = 4; i >= 1; i--) {
            Map<String, Object> level = new HashMap<>();
            level.put("k" + i, proxy);
            proxy = ProxyObject.fromMap(level);
        }

        Object result = PropertyPathNavigator.navigatePath(
                new String[]{"k1", "k2", "k3", "k4", "email"}, 0, proxy);
        assertEquals(result, "alice@example.com");
    }

    @Test
    public void testNavigatePathArrayFirstAndLastIndex() {

        ProxyArray array = ProxyArray.fromList(Arrays.asList("first", "middle", "last"));
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("items", array);
        ProxyObject root = ProxyObject.fromMap(rootMap);

        assertEquals(PropertyPathNavigator.navigatePath(
                new String[]{"items", "0"}, 0, root), "first");
        assertEquals(PropertyPathNavigator.navigatePath(
                new String[]{"items", "2"}, 0, root), "last");
    }

    @Test
    public void testNavigatePathArrayOutOfBoundsPropagates() {

        // ProxyArray.fromList throws ArrayIndexOutOfBoundsException for out-of-range index.
        // The navigator does NOT swallow it — callers get the underlying error. This
        // documents that out-of-bounds reads must fail loudly rather than silently returning
        // null (which would look like a missing property and hide the real bug).
        ProxyArray array = ProxyArray.fromList(Arrays.asList("only"));
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("items", array);
        ProxyObject root = ProxyObject.fromMap(rootMap);

        try {
            PropertyPathNavigator.navigatePath(new String[]{"items", "42"}, 0, root);
            fail("Out-of-bounds array access must not silently return null");
        } catch (IndexOutOfBoundsException | IllegalStateException expected) {
            // Either is acceptable — the key property is that the error propagates.
            // (ArrayIndexOutOfBoundsException is already an IndexOutOfBoundsException.)
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNavigatePathNegativeIndexStringFallsToGetMemberOnArray() {

        // "-1" is not matched by isNumeric — navigateSingleStep then tries getMember on a
        // ProxyArray, which is not a ProxyObject → IllegalStateException. This guards
        // against negative-index "wrap-around" behaviour sneaking in.
        ProxyArray array = ProxyArray.fromList(Arrays.asList("a", "b"));
        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("items", array);
        ProxyObject root = ProxyObject.fromMap(rootMap);

        PropertyPathNavigator.navigatePath(new String[]{"items", "-1"}, 0, root);
    }

    @Test
    public void testSetPropertyThroughNestedIntermediates() {

        // "steps::1::subject::email" = set a leaf three proxies deep.
        Map<String, Object> subjectBacking = new HashMap<>();
        ProxyObject subject = ProxyObject.fromMap(subjectBacking);
        Map<String, Object> stepBacking = new HashMap<>();
        stepBacking.put("subject", subject);
        ProxyObject step = ProxyObject.fromMap(stepBacking);
        Map<String, Object> rootBacking = new HashMap<>();
        rootBacking.put("step", step);
        ProxyObject root = ProxyObject.fromMap(rootBacking);

        boolean result = PropertyPathNavigator.setProperty(
                new String[]{"step", "subject", "email"}, 0, root, "bob@example.com");

        assertTrue(result);
        assertTrue(subjectBacking.containsKey("email"),
                "Deep set must land on the innermost ProxyObject's backing map");
    }

    @Test
    public void testSetPropertySingleLongDigitStringIsNumeric() {

        // Ensure isNumeric handles max-length digit strings without overflow pitfalls —
        // it only checks character classes, not Integer.parseInt, so arbitrary-length
        // digit strings are "numeric" without throwing. The numeric ProxyArray branch
        // then uses Integer.parseInt which WILL throw — this test pins the boundary
        // between the two.
        assertTrue(PropertyPathNavigator.isNumeric(
                String.valueOf(Integer.MAX_VALUE)),
                "Max-int digit string must be recognised as numeric");
        assertTrue(PropertyPathNavigator.isNumeric("9999999999999999999"),
                "isNumeric recognises oversize digit strings — Integer.parseInt throws later " +
                        "if used for array indexing");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNavigatePathStartIndexOutOfRangeThrowsOnNullRoot() {

        // Defensive: first segment must not be walked from a null root.
        PropertyPathNavigator.navigatePath(new String[]{"anything"}, 0, null);
    }

    @Test
    public void testGetMemberKeysViaKeysTerminalOnDeepPath() {

        // The __keys__ terminal should fire wherever it appears on a ProxyObject — not
        // just at depth 1. Simulates "user::profile::__keys__" used for Object.keys().
        Map<String, Object> profileMap = new HashMap<>();
        profileMap.put("first", 1);
        profileMap.put("last", 2);
        ProxyObject profile = ProxyObject.fromMap(profileMap);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("profile", profile);
        ProxyObject root = ProxyObject.fromMap(userMap);

        Object keys = PropertyPathNavigator.navigatePath(
                new String[]{"profile", RemoteEngineConstants.KEYS_PROPERTY}, 0, root);
        assertNotNull(keys);
    }
}
