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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link ArgumentAdapter}.
 * <p>
 * Covers the protobuf &rarr; host-method argument coercion contract:
 * scalar numeric widening/narrowing, boolean string parsing, list/array shape conversion,
 * {@link ArgumentAdapter#coerceMapNumberTypes} whole-Double &rarr; Integer promotion, and
 * {@link ArgumentAdapter#adaptArgumentsForMethod} varargs/fixed-arity dispatch.
 */
public class ArgumentAdapterTest {

    private ArgumentAdapter adapter;

    // Reflection target methods — covering fixed-arity, varargs, and typed parameters.
    // Signatures mirror real conditional-auth host functions:
    //   executeStep(Integer, Object...)           → stepExecutor(int, objectVarArgs)
    //   httpGet(String, Object...)                → strHeadObjectVarArgs
    //   setCookie(JsServletResponse, String, Object...)  → objectVarArgs covers optionsMap
    //   promptIdentifier(int, Object...)          → primInt + objectVarArgs
    //   longPrim is a sanity target for raw long widening.
    @SuppressWarnings("unused")
    static final class TargetMethods {

        public void fixedTwo(String a, int b) { }

        public void withVarArgs(String head, String... tail) { }

        public void withObjectVarArgs(Object... values) { }

        public void stepExecutor(int stepId, Object... parameterMap) { }

        public void primitivesOnly(int i, long l, double d, boolean b) { }

        public void objectBoxes(Integer i, Long l, Double d, Boolean b) { }
    }

    @BeforeMethod
    public void setUp() {

        // A bare context is sufficient: only reconstructFromProxy (covered by integration
        // tests) exercises the context — the adapter branches under test do not.
        adapter = new ArgumentAdapter(new AuthenticationContext());
    }

    // ===== scalar coercion =====

    @Test
    public void testNullArgumentPassesThrough() {

        assertNull(adapter.adaptSingleArgument(null, String.class));
    }

    @DataProvider(name = "integerCoercions")
    public Object[][] integerCoercions() {

        return new Object[][]{
                {3.0, 3},
                {3L, 3},
                {3, 3},
                {"42", 42}
        };
    }

    @Test(dataProvider = "integerCoercions")
    public void testIntegerCoercion(Object input, int expected) {

        Object result = adapter.adaptSingleArgument(input, Integer.class);
        assertEquals(result, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIntegerCoercionInvalidStringThrows() {

        adapter.adaptSingleArgument("not-a-number", Integer.class);
    }

    @Test
    public void testLongCoercionFromDouble() {

        Object result = adapter.adaptSingleArgument(7.0, Long.class);
        assertEquals(result, 7L);
    }

    @Test
    public void testDoubleCoercionFromInteger() {

        Object result = adapter.adaptSingleArgument(5, Double.class);
        assertEquals(result, 5.0);
    }

    @DataProvider(name = "booleans")
    public Object[][] booleans() {

        return new Object[][]{
                {true, true},
                {false, false},
                {"true", true},
                {"TRUE", true},
                {" true ", true},
                {"false", false},
                {"False", false}
        };
    }

    @Test(dataProvider = "booleans")
    public void testBooleanCoercion(Object input, boolean expected) {

        Object result = adapter.adaptSingleArgument(input, Boolean.class);
        assertEquals(result, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBooleanCoercionInvalidStringThrows() {

        adapter.adaptSingleArgument("maybe", Boolean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBooleanCoercionWrongTypeThrows() {

        // Non-Boolean, non-String inputs must fail — the alternative would silently
        // accept anything truthy and produce incorrect host calls.
        adapter.adaptSingleArgument(42, Boolean.class);
    }

    @Test
    public void testStringCoercionUsesToString() {

        assertEquals(adapter.adaptSingleArgument(7, String.class), "7");
        assertEquals(adapter.adaptSingleArgument(true, String.class), "true");
    }

    // ===== collection / array coercion =====

    @Test
    public void testListPassesThroughWhenTargetIsList() {

        List<String> input = Arrays.asList("x", "y");
        Object result = adapter.adaptSingleArgument(input, List.class);
        assertEquals(result, input);
    }

    @Test
    public void testObjectArrayConvertedToListWhenTargetIsList() {

        Object result = adapter.adaptSingleArgument(new String[]{"a", "b"}, List.class);
        assertTrue(result instanceof List);
        assertEquals(result, Arrays.asList("a", "b"));
    }

    @Test
    public void testListConvertedToStringArrayWhenTargetIsStringArray() {

        Object result = adapter.adaptSingleArgument(Arrays.asList("a", "b"), String[].class);
        assertTrue(result instanceof String[]);
        assertEquals(((String[]) result)[0], "a");
        assertEquals(((String[]) result)[1], "b");
    }

    @Test
    public void testListConvertedToObjectArrayWhenTargetIsObjectArray() {

        Object result = adapter.adaptSingleArgument(Arrays.asList(1, 2), Object[].class);
        assertTrue(result instanceof Object[]);
        assertEquals(((Object[]) result)[0], 1);
    }

    // ===== Object target + Map number coercion =====

    @Test
    public void testMapForObjectTargetCoercesWholeDoublesToInteger() {

        // Sidecar sends numbers as Double; host signature is Object and expects Integer
        // where the JS value is a whole number — this is the bug the adapter is fixing.
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("maxAge", 3600.0);
        input.put("enabled", true);
        input.put("ratio", 1.5);

        Object adapted = adapter.adaptSingleArgument(input, Object.class);
        assertTrue(adapted instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) adapted;

        assertEquals(result.get("maxAge"), 3600, "Whole Double must be promoted to Integer");
        assertEquals(result.get("enabled"), true, "Booleans must pass through unchanged");
        assertEquals(result.get("ratio"), 1.5, "Fractional Doubles must stay Double");
    }

    @Test
    public void testCoerceMapNumberTypesPromotesNestedMap() {

        Map<String, Object> inner = new HashMap<>();
        inner.put("cookieMaxAge", 600.0);
        Map<String, Object> outer = new HashMap<>();
        outer.put("settings", inner);

        Map<String, Object> result = adapter.coerceMapNumberTypes(outer);
        @SuppressWarnings("unchecked")
        Map<String, Object> innerResult = (Map<String, Object>) result.get("settings");
        assertEquals(innerResult.get("cookieMaxAge"), 600,
                "Nested whole Double must be promoted to Integer");
    }

    @Test
    public void testCoerceMapNumberTypesUsesLongForOutOfIntRange() {

        double bigWhole = ((double) Integer.MAX_VALUE) + 1.0;
        Map<String, Object> input = new HashMap<>();
        input.put("bigId", bigWhole);

        Map<String, Object> result = adapter.coerceMapNumberTypes(input);
        assertEquals(result.get("bigId"), (long) bigWhole,
                "Whole Double beyond int range must become Long, not Integer");
    }

    @Test
    public void testCoerceMapNumberTypesLeavesFractionalDoubles() {

        Map<String, Object> input = new HashMap<>();
        input.put("ratio", 0.5);
        Map<String, Object> result = adapter.coerceMapNumberTypes(input);
        assertEquals(result.get("ratio"), 0.5);
    }

    // ===== reflected method dispatch =====

    @Test
    public void testAdaptArgumentsForFixedArityMethod() throws Exception {

        Method m = TargetMethods.class.getMethod("fixedTwo", String.class, int.class);
        Object[] adapted = adapter.adaptArgumentsForMethod(m, new Object[]{"label", 9.0});

        assertEquals(adapted.length, 2);
        assertEquals(adapted[0], "label");
        assertEquals(adapted[1], 9);     // Double coerced to int.
    }

    @Test
    public void testAdaptArgumentsForVarArgsMethod() throws Exception {

        Method m = TargetMethods.class.getMethod("withVarArgs", String.class, String[].class);
        Object[] adapted = adapter.adaptArgumentsForMethod(
                m, new Object[]{"head", "t1", "t2"});

        assertEquals(adapted.length, 2);
        assertEquals(adapted[0], "head");
        assertTrue(adapted[1] instanceof String[]);
        String[] tail = (String[]) adapted[1];
        assertEquals(tail.length, 2);
        assertEquals(tail[0], "t1");
        assertEquals(tail[1], "t2");
    }

    @Test
    public void testAdaptArgumentsForEmptyVarArgsTail() throws Exception {

        Method m = TargetMethods.class.getMethod("withVarArgs", String.class, String[].class);
        Object[] adapted = adapter.adaptArgumentsForMethod(m, new Object[]{"only-head"});

        assertEquals(adapted.length, 2);
        assertEquals(adapted[0], "only-head");
        assertNotNull(adapted[1]);
        assertEquals(((String[]) adapted[1]).length, 0,
                "An empty varargs tail must be a zero-length array, not null");
    }

    @Test
    public void testAdaptArgumentsNullVarArgElementThrows() throws Exception {

        Method m = TargetMethods.class.getMethod("withVarArgs", String.class, String[].class);
        try {
            adapter.adaptArgumentsForMethod(m, new Object[]{"head", null});
            fail("Null elements in varargs must fail fast — they masquerade as missing args");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("varargs"));
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAdaptArgumentsMissingFixedArgumentThrows() throws Exception {

        Method m = TargetMethods.class.getMethod("fixedTwo", String.class, int.class);
        adapter.adaptArgumentsForMethod(m, new Object[]{"only-one"});
    }

    @Test
    public void testAdaptArgumentsForNullArgsArrayOnZeroArity() throws Exception {

        Method m = TargetMethods.class.getMethod("withVarArgs", String.class, String[].class);
        // Null args with a varargs-only signature would be ambiguous; we exercise the
        // guard that normalises null -> empty array.
        try {
            adapter.adaptArgumentsForMethod(m, null);
            fail("Expected IllegalArgumentException: head param is still required");
        } catch (IllegalArgumentException expected) {
            // expected — adapter normalised null args to Object[0], then hit "missing arg at 0".
            assertTrue(expected.getMessage().contains("Missing argument"));
        }
    }

    // ========================================================================
    // Edge cases reproducing identity-conditional-auth-functions host signatures.
    // ========================================================================

    @Test
    public void testPrimitiveIntTargetFromDouble() {

        // The sidecar returns all numbers as Double. A host method taking `int` must
        // receive an Integer — Method.invoke unboxes to int. Returning a Double would
        // throw IllegalArgumentException at invoke time.
        Object result = adapter.adaptSingleArgument(5.0, int.class);
        assertTrue(result instanceof Integer, "int.class target must produce Integer, not Double");
        assertEquals(result, 5);
    }

    @Test
    public void testPrimitiveLongTargetFromDouble() {

        Object result = adapter.adaptSingleArgument(7.0, long.class);
        assertTrue(result instanceof Long, "long.class target must produce Long");
        assertEquals(result, 7L);
    }

    @Test
    public void testPrimitiveDoubleTargetFromInteger() {

        Object result = adapter.adaptSingleArgument(3, double.class);
        assertTrue(result instanceof Double);
        assertEquals(result, 3.0);
    }

    @Test
    public void testPrimitiveBooleanTarget() {

        Object result = adapter.adaptSingleArgument(true, boolean.class);
        assertTrue(result instanceof Boolean);
        assertEquals(result, true);
    }

    @Test
    public void testFractionalDoubleTruncatesWhenCoercedToInteger() {

        // Current contract: fractional values truncate (Number.intValue()). Documents
        // the behaviour — if this ever changes to rounding/throw, the test will catch it.
        Object result = adapter.adaptSingleArgument(1.9, Integer.class);
        assertEquals(result, 1, "Fractional Double must truncate toward zero");

        Object negResult = adapter.adaptSingleArgument(-1.9, Integer.class);
        assertEquals(negResult, -1, "Negative fractional truncation follows Number.intValue()");
    }

    @Test
    public void testLongToIntegerNarrowingTruncates() {

        // Long beyond int range silently narrows via intValue() — documents the lossy
        // behaviour. Host functions expecting int must not receive values they can't hold,
        // but the adapter is not the place that validates range; Java does.
        long big = ((long) Integer.MAX_VALUE) + 1L;
        Object result = adapter.adaptSingleArgument(big, Integer.class);
        assertTrue(result instanceof Integer);
        // intValue() on Integer.MAX_VALUE + 1 wraps to Integer.MIN_VALUE — documents the
        // fact that overflow is silent. Guardrails belong at the host signature boundary.
        assertEquals(result, Integer.MIN_VALUE);
    }

    @Test
    public void testExecuteStepShapeVarArgsWithMapCoercion() throws Exception {

        // Real call: executeStep(1, { onSuccess: fn, onFail: fn, authenticationOptions: [...] })
        // The options map arrives with every Number as Double. Object... varargs tail
        // forces each element through adaptSingleArgument(raw, Object.class) which
        // must coerce whole-Doubles inside the map.
        Method m = TargetMethods.class.getMethod("stepExecutor", int.class, Object[].class);

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("timeout", 30.0);          // whole Double — must become Integer
        options.put("retries", 3.0);           // whole Double
        options.put("ratio", 0.5);             // fractional — stays Double

        Object[] adapted = adapter.adaptArgumentsForMethod(m, new Object[]{1.0, options});

        assertEquals(adapted[0], 1, "Step id Double must coerce to int");
        Object[] varArgs = (Object[]) adapted[1];
        assertEquals(varArgs.length, 1);
        @SuppressWarnings("unchecked")
        Map<String, Object> coerced = (Map<String, Object>) varArgs[0];
        assertEquals(coerced.get("timeout"), 30, "Options map whole-Doubles must coerce to Integer");
        assertEquals(coerced.get("retries"), 3);
        assertEquals(coerced.get("ratio"), 0.5, "Fractional Doubles must stay as Doubles");
    }

    @Test
    public void testCookieOptionsMapCoercionPath() throws Exception {

        // setCookie(response, name, value, optionsMap) — optionsMap is an Object... element.
        // The real production bug was max-age = 3600.0 arriving as Double into a host
        // function that read `(Integer) options.get("max-age")`.
        Method m = TargetMethods.class.getMethod("withObjectVarArgs", Object[].class);

        Map<String, Object> cookieOpts = new LinkedHashMap<>();
        cookieOpts.put("max-age", 3600.0);
        cookieOpts.put("secure", true);
        cookieOpts.put("path", "/");
        cookieOpts.put("sign", true);

        Object[] adapted = adapter.adaptArgumentsForMethod(m, new Object[]{cookieOpts});
        Object[] varArgs = (Object[]) adapted[0];
        @SuppressWarnings("unchecked")
        Map<String, Object> out = (Map<String, Object>) varArgs[0];

        assertEquals(out.get("max-age"), 3600, "max-age must reach host code as Integer");
        assertEquals(out.get("secure"), true);
        assertEquals(out.get("path"), "/");
        assertEquals(out.get("sign"), true);
    }

    @Test
    public void testCoerceMapNumberTypesProducesMutableMap() {

        // Production calls may mutate the returned map. Protobuf-backed maps are immutable;
        // the adapter guarantees a HashMap result. Test puts a new key after coercion.
        Map<String, Object> input = Collections.singletonMap("n", 1.0);
        Map<String, Object> result = adapter.coerceMapNumberTypes(input);
        result.put("post-coercion", "ok"); // must not throw UnsupportedOperationException
        assertEquals(result.get("n"), 1);
        assertEquals(result.get("post-coercion"), "ok");
    }

    @Test
    public void testCoerceMapNumberTypesDoesNotMutateInput() {

        // Verifies the adapter returns a new Map rather than mutating the caller's input —
        // important because the caller's map may be shared across callbacks.
        Map<String, Object> input = new HashMap<>();
        input.put("n", 1.0);
        input.put("fractional", 0.5);

        adapter.coerceMapNumberTypes(input);

        assertEquals(input.get("n"), 1.0, "Input map must remain untouched — Double still Double");
        assertEquals(input.get("fractional"), 0.5);
    }

    @Test
    public void testNumberCoercionAcceptsBoxedAndPrimitiveSources() {

        // All four boxed numeric types must satisfy the Number check equally.
        assertEquals(adapter.adaptSingleArgument(Byte.valueOf((byte) 5), Integer.class), 5);
        assertEquals(adapter.adaptSingleArgument(Short.valueOf((short) 5), Integer.class), 5);
        assertEquals(adapter.adaptSingleArgument(Float.valueOf(5.0f), Integer.class), 5);
    }

    @Test
    public void testObjectTargetPassesThroughListUnchanged() {

        // A List under Object.class target should NOT be run through coerceMapNumberTypes
        // (that's Map-only). The list passes through unchanged.
        List<Object> input = Arrays.asList(1.0, 2.0, 3.0);
        Object result = adapter.adaptSingleArgument(input, Object.class);
        assertSame(result, input);
    }

    @Test
    public void testObjectTargetPassesThroughStringUnchanged() {

        Object result = adapter.adaptSingleArgument("hello", Object.class);
        assertEquals(result, "hello");
    }

    @Test
    public void testAdaptArgumentsCanHandleAllPrimitiveTypesInOneCall() throws Exception {

        // End-to-end: every primitive target must coerce correctly when bundled.
        Method m = TargetMethods.class.getMethod(
                "primitivesOnly", int.class, long.class, double.class, boolean.class);
        Object[] adapted = adapter.adaptArgumentsForMethod(
                m, new Object[]{5.0, 9_000_000_000.0, 3.14, true});

        assertEquals(adapted[0], 5);
        assertEquals(adapted[1], 9_000_000_000L);
        assertEquals(adapted[2], 3.14);
        assertEquals(adapted[3], true);
    }

    @Test
    public void testVarArgsWithSingleMapElement() throws Exception {

        // Regression: ensure a single-element varargs tail carrying a Map does not
        // accidentally wrap the Map INTO another Object[] when already inside one.
        Method m = TargetMethods.class.getMethod("withObjectVarArgs", Object[].class);
        Map<String, Object> opts = Collections.singletonMap("k", 1.0);
        Object[] adapted = adapter.adaptArgumentsForMethod(m, new Object[]{opts});

        Object[] varArgs = (Object[]) adapted[0];
        assertEquals(varArgs.length, 1);
        assertTrue(varArgs[0] instanceof Map, "Map must stay as a Map, not be re-wrapped in Object[]");
    }

    @Test
    public void testMissingArgumentErrorNamesTheMethod() throws Exception {

        // Error message should help the operator find the offending host call.
        Method m = TargetMethods.class.getMethod("fixedTwo", String.class, int.class);
        try {
            adapter.adaptArgumentsForMethod(m, new Object[]{"only"});
            fail("Expected IllegalArgumentException for missing argument");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("fixedTwo"),
                    "Error message must name the method — received: " + expected.getMessage());
        }
    }
}
