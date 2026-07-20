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

package org.wso2.carbon.identity.flow.extension.executor;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link PathTypeAnnotationUtil}: trailing-annotation stripping and the
 * per-object attribute limit validation.
 */
public class PathTypeAnnotationUtilTest {

    @Test
    public void testStripAnnotationNullPath() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation(null);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test
    public void testStripAnnotationWithoutAnnotation() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/user/claims[uri=http://wso2.org/claims/x]");
        assertEquals(result[0], "/user/claims[uri=http://wso2.org/claims/x]");
        assertNull(result[1]);
    }

    @Test
    public void testStripAnnotationWithAnnotation() {

        String[] result = PathTypeAnnotationUtil.stripAnnotation("/user/claims[uri=x]{[string]}");
        assertEquals(result[0], "/user/claims[uri=x]");
        assertEquals(result[1], "[string]");
    }

    @Test
    public void testValidateAnnotationLimitsNullOrEmpty() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits(null));
        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits(""));
    }

    @Test
    public void testValidateAnnotationLimitsNonComplex() {

        // No colon -> not a complex/object annotation -> always valid.
        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("string"));
        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("[string]"));
    }

    @Test
    public void testValidateAnnotationLimitsWithinLimit() {

        assertTrue(PathTypeAnnotationUtil.validateAnnotationLimits("[a:string,b:int,c:boolean]"));
    }

    @Test
    public void testValidateAnnotationLimitsExceedsLimit() {

        StringBuilder complex = new StringBuilder("[");
        for (int i = 0; i < 11; i++) {
            if (i > 0) {
                complex.append(",");
            }
            complex.append("attr").append(i).append(":string");
        }
        complex.append("]");

        assertFalse(PathTypeAnnotationUtil.validateAnnotationLimits(complex.toString()));
    }
}
