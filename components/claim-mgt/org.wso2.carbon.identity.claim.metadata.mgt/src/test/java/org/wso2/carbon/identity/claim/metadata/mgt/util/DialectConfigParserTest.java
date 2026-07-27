/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.claim.metadata.mgt.util;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

public class DialectConfigParserTest {

    private static MockedStatic<IdentityUtil> identityUtilMock;

    @BeforeMethod
    public void setup() throws Exception {

        identityUtilMock = mockStatic(IdentityUtil.class);
        identityUtilMock.when(IdentityUtil::getIdentityConfigDirPath)
                .thenReturn("src/test/resources/testSchemas/valid");

        resetSingletonInstance();
    }

    @Test
    public void testGetInstance() {

        DialectConfigParser instance1 = DialectConfigParser.getInstance();
        DialectConfigParser instance2 = DialectConfigParser.getInstance();

        Assert.assertNotNull(instance1);
        Assert.assertSame(instance1, instance2, "getInstance() should return the same instance");
    }

    @Test
    public void testBuildConfigurationSuccess() {

        DialectConfigParser parser = DialectConfigParser.getInstance();
        Map<String, String> additions = parser.getAdditionsToDefaultDialects();
        Map<String, String> removals = parser.getRemovalsFromDefaultDialects();

        Assert.assertEquals(additions.size(), 2, "Should correctly parse AddSchema elements");
        Assert.assertEquals(removals.size(), 1, "Should correctly parse RemoveSchema elements");
    }

    @AfterMethod
    public void tearDown() {

        identityUtilMock.close();
    }

    /**
     * Reset the singleton instance using reflection (compatible with Java 12+).
     * Uses Unsafe API to modify static final fields.
     *
     * @throws Exception If an error occurs.
     */
    private void resetSingletonInstance() throws Exception {

        Class<?> innerClass = Class.forName("org.wso2.carbon.identity.claim.metadata.mgt.util." +
                "DialectConfigParser$SchemaConfigParserHolder");
        Field instanceField = innerClass.getDeclaredField("schemaConfigParser");
        instanceField.setAccessible(true);

        // Use Unsafe to modify static final fields in Java 12+
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Constructor<DialectConfigParser> constructor = DialectConfigParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DialectConfigParser newInstance = constructor.newInstance();

        Object fieldBase = unsafe.staticFieldBase(instanceField);
        long fieldOffset = unsafe.staticFieldOffset(instanceField);
        unsafe.putObject(fieldBase, fieldOffset, newInstance);
    }
}
