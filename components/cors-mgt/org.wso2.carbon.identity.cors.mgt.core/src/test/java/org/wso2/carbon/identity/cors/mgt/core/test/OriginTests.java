/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.cors.mgt.core.test;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.cors.mgt.core.model.Origin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the base origin class.
 */
public class OriginTests {

    @Test
    public void testOrigin() {

        String uri = "http://example.com";
        Origin o = new Origin(uri);

        assertEquals(o.toString(), uri);
        assertEquals(o.hashCode(), uri.hashCode());
    }

    @Test
    public void testOriginEquality() {

        String uri = "http://example.com";
        Origin o1 = new Origin(uri);
        Origin o2 = new Origin(uri);

        assertTrue(o1.equals(o2));
    }

    @Test
    public void testOriginInequality() {

        String uri1 = "http://example.com";
        String uri2 = "HTTP://EXAMPLE.COM";
        Origin o1 = new Origin(uri1);
        Origin o2 = new Origin(uri2);

        assertFalse(o1.equals(o2));
    }

    @Test
    public void testOriginInequalityNull() {

        assertFalse(new Origin("http://example.com").equals(null));
    }
}
