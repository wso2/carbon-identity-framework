/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.compatibility.settings.core.provider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.compatibility.settings.core.constant.IdentityCompatibilitySettingsConstants.ErrorMessages;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingException;
import org.wso2.carbon.identity.compatibility.settings.core.exception.CompatibilitySettingServerException;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaData;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataEntry;
import org.wso2.carbon.identity.compatibility.settings.core.model.metadata.CompatibilitySettingMetaDataGroup;

import java.io.File;
import java.net.URL;
import java.time.Instant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link FileBasedStaticMetaDataProvider}.
 */
public class FileBasedStaticMetaDataProviderTest {

    private static final String TEST_METADATA_FILE_NAME = "test-compatibility-settings-metadata.json";
    private static final String INVALID_METADATA_FILE_NAME = "invalid-compatibility-settings-metadata.json";
    private static final String INVALID_DATETIME_METADATA_FILE_NAME =
            "invalid-datetime-compatibility-settings-metadata.json";
    private static final String NON_EXISTENT_FILE_NAME = "non-existent-file.json";

    private static final String SETTING_GROUP_AUTHENTICATION = "authentication";
    private static final String SETTING_GROUP_USER_MANAGEMENT = "userManagement";
    private static final String SETTING_GROUP_NON_EXISTENT = "nonExistentGroup";

    private static final String SETTING_LEGACY_AUTH_FLOW = "enableLegacyAuthFlow";
    private static final String SETTING_BACKWARD_COMPATIBLE_SESSION = "enableBackwardCompatibleSession";
    private static final String SETTING_LEGACY_USER_STORE = "useLegacyUserStore";
    private static final String SETTING_NON_EXISTENT = "nonExistentSetting";

    private String testResourcePath;

    @BeforeClass
    public void setUp() {

        URL resourceUrl = getClass().getClassLoader().getResource(TEST_METADATA_FILE_NAME);
        if (resourceUrl != null) {
            File file = new File(resourceUrl.getFile());
            testResourcePath = file.getParent();
        }
    }

    /**
     * Test creating FileBasedStaticMetaDataProvider with valid file path.
     */
    @Test
    public void testCreateProviderWithValidFile() throws CompatibilitySettingServerException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        assertNotNull(provider);
        assertEquals(provider.getPriority(), 10);
    }

    /**
     * Test getFilePath method returns correct path.
     */
    @Test
    public void testGetFilePath() throws CompatibilitySettingServerException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        String filePath = provider.getFilePath();
        assertNotNull(filePath);
        assertTrue(filePath.endsWith(TEST_METADATA_FILE_NAME));
    }

    /**
     * Test getMetaData returns complete metadata.
     */
    @Test
    public void testGetMetaData() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaData metaData = provider.getMetaData();

        assertNotNull(metaData);
        assertNotNull(metaData.getSettingsMetaData());
        assertEquals(metaData.getSettingsMetaData().size(), 2);
        assertTrue(metaData.getSettingsMetaData().containsKey(SETTING_GROUP_AUTHENTICATION));
        assertTrue(metaData.getSettingsMetaData().containsKey(SETTING_GROUP_USER_MANAGEMENT));
    }

    /**
     * Test getMetaData with settingGroup parameter returns correct group.
     */
    @Test
    public void testGetMetaDataWithSettingGroup() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataGroup authGroup = provider.getMetaDataByGroup(SETTING_GROUP_AUTHENTICATION);

        assertNotNull(authGroup);
        assertNotNull(authGroup.getSettingMetaDataEntry(SETTING_LEGACY_AUTH_FLOW));
        assertNotNull(authGroup.getSettingMetaDataEntry(SETTING_BACKWARD_COMPATIBLE_SESSION));
    }

    /**
     * Test getMetaData with settingGroup returns null when setting group doesn't exist.
     */
    @Test
    public void testGetMetaDataWithNonExistentSettingGroup() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataGroup result = provider.getMetaDataByGroup(SETTING_GROUP_NON_EXISTENT);

        assertNull(result);
    }

    /**
     * Test getMetaData with settingGroup and setting parameters returns correct entry.
     */
    @Test
    public void testGetMetaDataWithSettingGroupAndSetting() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataEntry entry = provider.getMetaDataByGroupAndSetting(
                SETTING_GROUP_AUTHENTICATION, SETTING_LEGACY_AUTH_FLOW);

        assertNotNull(entry);
        assertEquals(entry.getTargetValue(), "true");
        assertEquals(entry.getDefaultValue(), "false");
        assertNotNull(entry.getTimestampReference());
        assertEquals(entry.getTimestampReference(), Instant.parse("2025-01-01T00:00:00Z"));
    }

    /**
     * Test getMetaData with non-existent setting returns null.
     */
    @Test
    public void testGetMetaDataWithNonExistentSetting() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataEntry entry = provider.getMetaDataByGroupAndSetting(
                SETTING_GROUP_AUTHENTICATION, SETTING_NON_EXISTENT);

        assertNull(entry);
    }

    /**
     * Test getMetaData for userManagement group.
     */
    @Test
    public void testGetMetaDataForUserManagementGroup() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataEntry entry = provider.getMetaDataByGroupAndSetting(
                SETTING_GROUP_USER_MANAGEMENT, SETTING_LEGACY_USER_STORE);

        assertNotNull(entry);
        assertEquals(entry.getTargetValue(), "true");
        assertEquals(entry.getDefaultValue(), "false");
        assertEquals(entry.getTimestampReference(), Instant.parse("2024-12-01T00:00:00Z"));
    }

    /**
     * Test creating provider with non-existent file throws CompatibilitySettingServerException.
     */
    @Test
    public void testCreateProviderWithNonExistentFileThrowsException() {

        try {
            new FileBasedStaticMetaDataProvider(
                    NON_EXISTENT_FILE_NAME, testResourcePath, File.separator);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(), ErrorMessages.ERROR_CODE_FILE_NOT_FOUND.getCode());
            assertTrue(e.getDescription().contains(NON_EXISTENT_FILE_NAME));
        }
    }

    /**
     * Test creating provider with invalid JSON file throws CompatibilitySettingServerException.
     */
    @Test
    public void testCreateProviderWithInvalidJsonThrowsException() {

        try {
            new FileBasedStaticMetaDataProvider(
                    INVALID_METADATA_FILE_NAME, testResourcePath, File.separator);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(), ErrorMessages.ERROR_CODE_ERROR_PARSING_JSON.getCode());
        } catch (Exception e) {
            fail("Expected CompatibilitySettingServerException, but got: " + e.getClass().getName());
        }
    }

    /**
     * Test creating provider with invalid date time format throws CompatibilitySettingServerException.
     */
    @Test
    public void testCreateProviderWithInvalidDateTimeThrowsException() {

        try {
            new FileBasedStaticMetaDataProvider(
                    INVALID_DATETIME_METADATA_FILE_NAME, testResourcePath, File.separator);
            fail("Expected CompatibilitySettingServerException to be thrown");
        } catch (CompatibilitySettingServerException e) {
            assertEquals(e.getErrorCode(), ErrorMessages.ERROR_CODE_ERROR_PARSING_DATE_TIME.getCode());
        } catch (Exception e) {
            fail("Expected CompatibilitySettingServerException, but got: " + e.getClass().getName());
        }
    }

    /**
     * Test default constructor throws exception when default file doesn't exist.
     */
    @Test(expectedExceptions = CompatibilitySettingServerException.class)
    public void testDefaultConstructor() throws CompatibilitySettingServerException {

        new FileBasedStaticMetaDataProvider();
    }

    /**
     * Test constructor with fileName and filePath array throws exception when file doesn't exist.
     */
    @Test(expectedExceptions = CompatibilitySettingServerException.class)
    public void testConstructorWithFileNameAndPathArray() throws CompatibilitySettingServerException {

        String[] pathSegments = {"test", "resources"};
        new FileBasedStaticMetaDataProvider("non-existent-file.json", pathSegments);
    }

    /**
     * Test getPriority returns correct value.
     */
    @Test
    public void testGetPriority() throws CompatibilitySettingServerException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        assertEquals(provider.getPriority(), 10);
    }

    /**
     * Test multiple settings in authentication group.
     */
    @Test
    public void testMultipleSettingsInGroup() throws CompatibilitySettingException {

        FileBasedStaticMetaDataProvider provider = new FileBasedStaticMetaDataProvider(
                TEST_METADATA_FILE_NAME, testResourcePath, File.separator);

        CompatibilitySettingMetaDataGroup authGroup = provider.getMetaDataByGroup(SETTING_GROUP_AUTHENTICATION);

        assertNotNull(authGroup);

        CompatibilitySettingMetaDataEntry legacyAuthEntry =
                authGroup.getSettingMetaDataEntry(SETTING_LEGACY_AUTH_FLOW);
        CompatibilitySettingMetaDataEntry backwardCompatEntry =
                authGroup.getSettingMetaDataEntry(SETTING_BACKWARD_COMPATIBLE_SESSION);

        assertNotNull(legacyAuthEntry);
        assertNotNull(backwardCompatEntry);

        // Verify different timestamp references.
        assertEquals(legacyAuthEntry.getTimestampReference(), Instant.parse("2025-01-01T00:00:00Z"));
        assertEquals(backwardCompatEntry.getTimestampReference(), Instant.parse("2025-06-15T12:00:00Z"));
    }
}
