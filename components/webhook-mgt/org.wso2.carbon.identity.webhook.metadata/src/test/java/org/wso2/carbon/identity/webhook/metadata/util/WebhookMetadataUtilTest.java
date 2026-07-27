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

package org.wso2.carbon.identity.webhook.metadata.util;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class WebhookMetadataUtilTest {

    private File tempCarbonHome;

    @BeforeMethod
    public void setUp() throws Exception {

        tempCarbonHome = Files.createTempDirectory("carbon_home").toFile();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, tempCarbonHome.getAbsolutePath());
        // Reset cached directory for each test
        java.lang.reflect.Field field = WebhookMetadataUtil.class.getDeclaredField("eventProfilesDirectory");
        field.setAccessible(true);
        field.set(null, null);
    }

    @AfterMethod
    public void tearDown() {

        System.clearProperty(CarbonBaseConstants.CARBON_HOME);
        tempCarbonHome.delete();
    }

    @Test
    public void testGetEventProfilesDirectory_DirectoryExists() throws Exception {

        File eventProfilesDir = new File(tempCarbonHome, "repository/resources/identity/eventprofiles");
        assertTrue(eventProfilesDir.mkdirs(), "Failed to create eventprofiles directory");

        Path result = WebhookMetadataUtil.getEventProfilesDirectory();
        assertEquals(result.toFile().getAbsolutePath(), eventProfilesDir.getAbsolutePath());
    }

    @Test(expectedExceptions = WebhookMetadataException.class)
    public void testGetEventProfilesDirectory_DirectoryNotExists() throws Exception {

        File eventProfilesDir = new File(tempCarbonHome, "repository/resources/identity/eventprofiles");
        if (eventProfilesDir.exists()) {
            eventProfilesDir.delete();
        }
        WebhookMetadataUtil.getEventProfilesDirectory();
    }

    @Test
    public void testGetEventProfilesDirectory_Caching() throws Exception {

        File eventProfilesDir = new File(tempCarbonHome, "repository/resources/identity/eventprofiles");
        assertTrue(eventProfilesDir.mkdirs(), "Failed to create eventprofiles directory");

        Path firstCall = WebhookMetadataUtil.getEventProfilesDirectory();
        Path secondCall = WebhookMetadataUtil.getEventProfilesDirectory();
        assertEquals(firstCall, secondCall, "Cached directory should be returned on subsequent calls");
    }
}
