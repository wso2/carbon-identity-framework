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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.test;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.cors.mgt.core.internal.util.SerializationUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit test cases for SerializationUtils.
 */
public class SerializationUtilTests {

    @Test
    public void testSerializeStringSet() {

        Set<String> values = new LinkedHashSet<>();
        values.add("apples");
        values.add("pears");
        values.add("oranges");

        String out = SerializationUtils.serializeStringSet(values);
        assertEquals(out, "apples;pears;oranges");
    }

    @Test
    public void testDeserializeStringSet() {

        Set<String> out = SerializationUtils.deserializeStringSet(null);
        assertEquals(0, out.size());

        out = SerializationUtils.deserializeStringSet("apples;pears;oranges");
        assertTrue(out.contains("apples"));
        assertTrue(out.contains("pears"));
        assertTrue(out.contains("oranges"));
        assertEquals(out.size(), 3);
    }
}
