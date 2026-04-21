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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedValue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link Serializer}.
 * <p>
 * Covers the Java &harr; protobuf {@code SerializedValue} conversion contract used by the
 * remote JS engine wire protocol: scalar round-trips, nested map/list structures, Java
 * array handling, function round-trip, and the thread-local session proxy cache contract.
 * Lives in the same package because {@code Serializer} is package-private.
 */
public class SerializerTest {

    @AfterMethod
    public void tearDownThreadLocal() {

        // The class stores the session proxy cache on a ThreadLocal. TestNG reuses
        // worker threads across tests, so leaking state between methods would cause
        // cross-test contamination. Always clear.
        Serializer.clearSessionProxyCache();
    }

    // ===== scalar round-trips =====

    @Test
    public void testNullRoundTripsAsNullValue() {

        SerializedValue sv = Serializer.toProto(null);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.NULL_VALUE);
        assertNull(Serializer.fromProto(sv));
    }

    @Test
    public void testStringRoundTrip() {

        SerializedValue sv = Serializer.toProto("hello");
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.STRING_VALUE);
        assertEquals(Serializer.fromProto(sv), "hello");
    }

    @Test
    public void testIntegerRoundTripsAsLong() {

        // protobuf SerializedValue has only INT_VALUE (int64) — Integers widen to Long.
        SerializedValue sv = Serializer.toProto(42);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.INT_VALUE);
        assertEquals(Serializer.fromProto(sv), 42L);
    }

    @Test
    public void testLongRoundTrip() {

        SerializedValue sv = Serializer.toProto(9_000_000_000L);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.INT_VALUE);
        assertEquals(Serializer.fromProto(sv), 9_000_000_000L);
    }

    @Test
    public void testDoubleRoundTrip() {

        SerializedValue sv = Serializer.toProto(3.14);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.DOUBLE_VALUE);
        assertEquals((Double) Serializer.fromProto(sv), 3.14, 0.0);
    }

    @Test
    public void testFloatWidensToDouble() {

        SerializedValue sv = Serializer.toProto(1.5f);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.DOUBLE_VALUE);
        // Round-trip loses the Float identity by design — compare as double.
        assertEquals((Double) Serializer.fromProto(sv), 1.5, 0.0);
    }

    @Test
    public void testBooleanRoundTrip() {

        SerializedValue trueSv = Serializer.toProto(true);
        assertEquals(trueSv.getValueCase(), SerializedValue.ValueCase.BOOL_VALUE);
        assertEquals(Serializer.fromProto(trueSv), true);

        SerializedValue falseSv = Serializer.toProto(false);
        assertEquals(falseSv.getValueCase(), SerializedValue.ValueCase.BOOL_VALUE);
        assertEquals(Serializer.fromProto(falseSv), false);
    }

    // ===== map / list / array =====

    @Test
    public void testMapRoundTrip() {

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("name", "alice");
        input.put("age", 30);
        input.put("active", true);

        SerializedValue sv = Serializer.toProto(input);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.MAP_VALUE);

        Object back = Serializer.fromProto(sv);
        assertTrue(back instanceof Map, "Map must deserialize to a java.util.Map");
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) back;
        assertEquals(result.get("name"), "alice");
        assertEquals(result.get("age"), 30L);      // int widens through proto
        assertEquals(result.get("active"), true);
    }

    @Test
    public void testListRoundTrip() {

        List<Object> input = Arrays.asList("a", "b", "c");
        SerializedValue sv = Serializer.toProto(input);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.ARRAY_VALUE);

        Object back = Serializer.fromProto(sv);
        assertTrue(back instanceof List);
        assertEquals(back, Arrays.asList("a", "b", "c"));
    }

    @Test
    public void testNestedMapAndList() {

        Map<String, Object> inner = new HashMap<>();
        inner.put("role", "admin");

        Map<String, Object> outer = new HashMap<>();
        outer.put("tags", Arrays.asList("x", "y"));
        outer.put("meta", inner);

        SerializedValue sv = Serializer.toProto(outer);
        @SuppressWarnings("unchecked")
        Map<String, Object> back = (Map<String, Object>) Serializer.fromProto(sv);
        assertEquals(back.get("tags"), Arrays.asList("x", "y"));
        @SuppressWarnings("unchecked")
        Map<String, Object> metaBack = (Map<String, Object>) back.get("meta");
        assertEquals(metaBack.get("role"), "admin");
    }

    @Test
    public void testObjectArraySerializesAsArrayValue() {

        // Java Object[] — e.g. HTTP request params — must serialize as ARRAY_VALUE.
        SerializedValue sv = Serializer.toProto(new String[]{"one", "two"});
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.ARRAY_VALUE);
        assertEquals(Serializer.fromProto(sv), Arrays.asList("one", "two"));
    }

    @Test
    public void testPrimitiveIntArraySerializesAsArrayValue() {

        // Primitive int[] — reflected in the special-case branch of toProto.
        SerializedValue sv = Serializer.toProto(new int[]{1, 2, 3});
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.ARRAY_VALUE);
        assertEquals(Serializer.fromProto(sv), Arrays.asList(1L, 2L, 3L));
    }

    // ===== function round-trip =====

    @Test
    public void testFunctionRoundTrip() {

        String src = "function(x){ return x * 2; }";
        GraalSerializableJsFunction fn = new GraalSerializableJsFunction(src, true);

        SerializedValue sv = Serializer.toProto(fn);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.FUNCTION_VALUE);
        assertEquals(sv.getFunctionValue().getSource(), src);

        Object back = Serializer.fromProto(sv);
        assertTrue(back instanceof GraalSerializableJsFunction);
        assertEquals(((GraalSerializableJsFunction) back).getSource(), src);
    }

    // ===== toProtoMap / fromProtoMap =====

    @Test
    public void testToProtoMapNullReturnsEmpty() {

        Map<String, SerializedValue> out = Serializer.toProtoMap(null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    public void testFromProtoMapNullReturnsEmpty() {

        Map<String, Object> out = Serializer.fromProtoMap(null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    public void testBindingsMapRoundTrip() {

        Map<String, Object> bindings = new LinkedHashMap<>();
        bindings.put("count", 5);
        bindings.put("label", "beta");
        bindings.put("enabled", false);

        Map<String, SerializedValue> wire = Serializer.toProtoMap(bindings);
        Map<String, Object> back = Serializer.fromProtoMap(wire);

        assertEquals(back.get("count"), 5L);
        assertEquals(back.get("label"), "beta");
        assertEquals(back.get("enabled"), false);
    }

    // ===== proxy-object deserialization =====

    @Test
    public void testFromProtoProxyObjectReturnsPlaceholderMap() {

        // Build a SerializedValue with a PROXY_OBJECT sub-message directly, mirroring what
        // the sidecar would send back for a lazy-loaded JsGraal wrapper.
        SerializedValue sv = SerializedValue.newBuilder()
                .setProxyObject(
                        org.wso2.carbon.identity.application.authentication.framework
                                .config.model.graph.graaljs.remote.proto.SerializedProxyObject.newBuilder()
                                .setType("authenticateduser")
                                .setReferenceId("ref-123")
                                .build())
                .build();

        Object back = Serializer.fromProto(sv);
        assertTrue(back instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> proxyMap = (Map<String, Object>) back;
        assertEquals(proxyMap.get(RemoteEngineConstants.PROXY_TYPE_FIELD), "authenticateduser");
        assertEquals(proxyMap.get(RemoteEngineConstants.REFERENCE_ID_FIELD), "ref-123");
    }

    // ===== unhandled type behaviour =====

    @Test
    public void testUnhandledTypeThrowsIllegalArgument() {

        // A plain non-Serializable POJO is passed through by GraalSerializer unchanged and
        // hits the final "Unhandled type" branch — serializer must fail loudly rather than
        // silently drop or mis-serialize.
        try {
            Serializer.toProto(new Object() {
                @Override
                public String toString() {
                    return "non-serializable-pojo";
                }
            });
            fail("Expected IllegalArgumentException for unhandled type");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unhandled type"),
                    "Error message should name the unhandled-type failure mode");
        }
    }

    @Test
    public void testFromProtoNullInputReturnsNull() {

        assertNull(Serializer.fromProto(null));
    }

    // ===== session proxy cache =====

    @Test
    public void testSessionProxyCacheIsolatedPerThread() throws Exception {

        // Peek directly at the ThreadLocal via reflection so the assertion is about the
        // actual per-thread slot, not a proxy of behavior.
        @SuppressWarnings("unchecked")
        ThreadLocal<Map<String, Object>> threadLocal =
                (ThreadLocal<Map<String, Object>>) readStaticField(Serializer.class, "sessionProxyCache");

        Map<String, Object> mainCache = new HashMap<>();
        mainCache.put("owner", "main");
        Serializer.setSessionProxyCache(mainCache);
        assertSame(threadLocal.get(), mainCache, "Main thread should see its own cache");

        AtomicReference<Map<String, Object>> workerSawFromTl = new AtomicReference<>();
        Thread worker = new Thread(() -> workerSawFromTl.set(threadLocal.get()));
        worker.start();
        worker.join(5000);

        assertNull(workerSawFromTl.get(),
                "Worker thread must see a null ThreadLocal slot — not main's cache");
    }

    @Test
    public void testClearSessionProxyCacheRemovesThreadLocalValue() throws Exception {

        @SuppressWarnings("unchecked")
        ThreadLocal<Map<String, Object>> threadLocal =
                (ThreadLocal<Map<String, Object>>) readStaticField(Serializer.class, "sessionProxyCache");

        Serializer.setSessionProxyCache(new HashMap<>());
        assertNotNull(threadLocal.get());

        Serializer.clearSessionProxyCache();
        assertNull(threadLocal.get(),
                "clearSessionProxyCache must null the ThreadLocal — a stale Map would leak across sessions");
    }

    @Test
    public void testSessionProxyCacheOverwriteReplaces() throws Exception {

        // RemoteJsEngine sets a fresh cache per evaluation — later set() must replace
        // the earlier one without accumulation or proxy of the prior map.
        @SuppressWarnings("unchecked")
        ThreadLocal<Map<String, Object>> threadLocal =
                (ThreadLocal<Map<String, Object>>) readStaticField(Serializer.class, "sessionProxyCache");

        Map<String, Object> first = new HashMap<>();
        Map<String, Object> second = new HashMap<>();

        Serializer.setSessionProxyCache(first);
        Serializer.setSessionProxyCache(second);

        assertSame(threadLocal.get(), second,
                "Second setSessionProxyCache must replace the first — stale cache would " +
                        "leak proxy refs across sessions");
    }

    // ========================================================================
    // Edge cases derived from real identity-conditional-auth-functions shapes.
    // ========================================================================

    @Test
    public void testMapWithFunctionValuesRoundTrip() {

        // Production shape: every httpGet/callChoreo/callElastic caller sends
        //   { onSuccess: function(ctx,data){...}, onFail: function(ctx,data){...} }
        // as `eventHandlers`. The map must serialize and the functions must come back
        // as GraalSerializableJsFunction instances — NOT as arbitrary maps.
        String onSuccessSrc = "function(ctx,data){ ctx.set('ok', true); }";
        String onFailSrc = "function(ctx,data){ ctx.set('ok', false); }";
        Map<String, Object> eventHandlers = new LinkedHashMap<>();
        eventHandlers.put("onSuccess", new GraalSerializableJsFunction(onSuccessSrc, true));
        eventHandlers.put("onFail", new GraalSerializableJsFunction(onFailSrc, true));

        SerializedValue sv = Serializer.toProto(eventHandlers);
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.MAP_VALUE);

        @SuppressWarnings("unchecked")
        Map<String, Object> back = (Map<String, Object>) Serializer.fromProto(sv);
        assertTrue(back.get("onSuccess") instanceof GraalSerializableJsFunction,
                "eventHandlers value must round-trip as a function, not a Map");
        assertEquals(((GraalSerializableJsFunction) back.get("onSuccess")).getSource(), onSuccessSrc);
        assertEquals(((GraalSerializableJsFunction) back.get("onFail")).getSource(), onFailSrc);
    }

    @Test
    public void testListWithFunctionElementRoundTrip() {

        String src = "function(x){ return x; }";
        List<Object> input = Arrays.asList(new GraalSerializableJsFunction(src, true), "tag");

        SerializedValue sv = Serializer.toProto(input);
        @SuppressWarnings("unchecked")
        List<Object> back = (List<Object>) Serializer.fromProto(sv);

        assertTrue(back.get(0) instanceof GraalSerializableJsFunction);
        assertEquals(((GraalSerializableJsFunction) back.get(0)).getSource(), src);
        assertEquals(back.get(1), "tag");
    }

    @Test
    public void testEmptyMapAndEmptyList() {

        // empty {} and [] are real payloads — Collections.emptyMap() is used as the
        // fallback response in asyncReturn.accept(context, emptyMap, outcome).
        SerializedValue mapSv = Serializer.toProto(Collections.emptyMap());
        assertEquals(mapSv.getValueCase(), SerializedValue.ValueCase.MAP_VALUE);
        Object mapBack = Serializer.fromProto(mapSv);
        assertTrue(mapBack instanceof Map);
        assertTrue(((Map<?, ?>) mapBack).isEmpty());

        SerializedValue listSv = Serializer.toProto(Collections.emptyList());
        assertEquals(listSv.getValueCase(), SerializedValue.ValueCase.ARRAY_VALUE);
        Object listBack = Serializer.fromProto(listSv);
        assertTrue(listBack instanceof List);
        assertTrue(((List<?>) listBack).isEmpty());
    }

    @Test
    public void testMapWithNullValueRoundTrip() {

        // JSON responses from external APIs may contain null fields — e.g. Choreo responses.
        // Serializer must keep the key and round-trip the null.
        Map<String, Object> input = new HashMap<>();
        input.put("present", "yes");
        input.put("missing", null);

        SerializedValue sv = Serializer.toProto(input);
        @SuppressWarnings("unchecked")
        Map<String, Object> back = (Map<String, Object>) Serializer.fromProto(sv);
        assertEquals(back.get("present"), "yes");
        assertTrue(back.containsKey("missing"), "Null-valued key must survive round-trip");
        assertNull(back.get("missing"));
    }

    @Test
    public void testListWithNullElementRoundTrip() {

        SerializedValue sv = Serializer.toProto(Arrays.asList("a", null, "c"));
        @SuppressWarnings("unchecked")
        List<Object> back = (List<Object>) Serializer.fromProto(sv);
        assertEquals(back.size(), 3);
        assertEquals(back.get(0), "a");
        assertNull(back.get(1), "Null array elements must round-trip as null, not be skipped");
        assertEquals(back.get(2), "c");
    }

    @Test
    public void testObjectArrayWithNullElement() {

        // HTTP response param arrays — e.g. String[] with null slots — shouldn't crash.
        SerializedValue sv = Serializer.toProto(new Object[]{"a", null});
        @SuppressWarnings("unchecked")
        List<Object> back = (List<Object>) Serializer.fromProto(sv);
        assertEquals(back.get(0), "a");
        assertNull(back.get(1));
    }

    @Test
    public void testDeeplyNestedStructure() {

        // Real JSON responses from external APIs can nest several levels.
        Map<String, Object> level3 = new HashMap<>();
        level3.put("leaf", 42);
        Map<String, Object> level2 = new HashMap<>();
        level2.put("level3", level3);
        Map<String, Object> level1 = new HashMap<>();
        level1.put("list", Arrays.asList(level2, "marker"));

        SerializedValue sv = Serializer.toProto(level1);
        @SuppressWarnings("unchecked")
        Map<String, Object> back = (Map<String, Object>) Serializer.fromProto(sv);

        @SuppressWarnings("unchecked")
        List<Object> listBack = (List<Object>) back.get("list");
        @SuppressWarnings("unchecked")
        Map<String, Object> l2Back = (Map<String, Object>) listBack.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> l3Back = (Map<String, Object>) l2Back.get("level3");
        assertEquals(l3Back.get("leaf"), 42L); // widens through proto
        assertEquals(listBack.get(1), "marker");
    }

    @Test
    public void testNegativeAndZeroNumericScalars() {

        assertEquals(Serializer.fromProto(Serializer.toProto(0)), 0L);
        assertEquals(Serializer.fromProto(Serializer.toProto(-1)), -1L);
        assertEquals(Serializer.fromProto(Serializer.toProto(-9_999_999_999L)), -9_999_999_999L);
        assertEquals((Double) Serializer.fromProto(Serializer.toProto(-3.14)), -3.14, 0.0);
        assertEquals((Double) Serializer.fromProto(Serializer.toProto(0.0)), 0.0, 0.0);
    }

    @Test
    public void testDoubleSpecialValues() {

        // NaN and Infinity are representable in protobuf double; must round-trip.
        Object nanBack = Serializer.fromProto(Serializer.toProto(Double.NaN));
        assertTrue(nanBack instanceof Double);
        assertTrue(Double.isNaN((Double) nanBack));

        Object posInf = Serializer.fromProto(Serializer.toProto(Double.POSITIVE_INFINITY));
        assertEquals(posInf, Double.POSITIVE_INFINITY);

        Object negInf = Serializer.fromProto(Serializer.toProto(Double.NEGATIVE_INFINITY));
        assertEquals(negInf, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testEmptyStringRoundTrip() {

        // Empty strings appear in eventHandler payloads — they're distinct from null.
        SerializedValue sv = Serializer.toProto("");
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.STRING_VALUE);
        assertEquals(Serializer.fromProto(sv), "");
    }

    @Test
    public void testPrimitiveBooleanArraySerializesAsArrayValue() {

        // Java reflective primitive-array branch — covers boolean[] specifically.
        SerializedValue sv = Serializer.toProto(new boolean[]{true, false, true});
        assertEquals(sv.getValueCase(), SerializedValue.ValueCase.ARRAY_VALUE);
        assertEquals(Serializer.fromProto(sv), Arrays.asList(true, false, true));
    }

    @Test
    public void testToProtoMapRoundTripPreservesEventHandlerMapShape() {

        // Exact shape sent in callChoreo / httpGet bindings map.
        Map<String, Object> bindings = new LinkedHashMap<>();
        bindings.put("endpointURL", "https://api.example.com");
        bindings.put("cookieOptions",
                mapOf("maxAge", 3600, "secure", true, "path", "/"));
        bindings.put("eventHandlers",
                mapOf("onSuccess",
                        new GraalSerializableJsFunction("function(c,d){}", true),
                        "onFail",
                        new GraalSerializableJsFunction("function(c,d){}", true)));

        Map<String, SerializedValue> wire = Serializer.toProtoMap(bindings);
        Map<String, Object> back = Serializer.fromProtoMap(wire);

        assertEquals(back.get("endpointURL"), "https://api.example.com");

        @SuppressWarnings("unchecked")
        Map<String, Object> cookieOptsBack = (Map<String, Object>) back.get("cookieOptions");
        assertEquals(cookieOptsBack.get("maxAge"), 3600L); // widens — coerceMapNumberTypes
        // handles the narrowing later in ArgumentAdapter.
        assertEquals(cookieOptsBack.get("secure"), true);
        assertEquals(cookieOptsBack.get("path"), "/");

        @SuppressWarnings("unchecked")
        Map<String, Object> handlersBack = (Map<String, Object>) back.get("eventHandlers");
        assertTrue(handlersBack.get("onSuccess") instanceof GraalSerializableJsFunction);
        assertTrue(handlersBack.get("onFail") instanceof GraalSerializableJsFunction);
    }

    // ===== helpers =====

    private static Map<String, Object> mapOf(Object... kv) {

        if ((kv.length & 1) != 0) {
            throw new IllegalArgumentException("mapOf requires even key/value count");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    private static Object readStaticField(Class<?> cls, String fieldName) throws Exception {

        Field f = cls.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }
}
