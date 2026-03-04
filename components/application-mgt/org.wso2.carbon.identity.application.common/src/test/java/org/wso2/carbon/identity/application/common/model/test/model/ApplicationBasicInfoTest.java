/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model.test.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the ApplicationBasicInfo model.
 */
@Test
public class ApplicationBasicInfoTest {

    /**
     * Test trimming of access URL when setting the access URL.
     */
    @Test(description = "Test trimming of access URL with leading and trailing whitespaces")
    public void testAccessUrlTrimming() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithWhitespace = "  https://example.com/app  ";
        String expectedUrl = "https://example.com/app";

        applicationBasicInfo.setAccessUrl(urlWithWhitespace);
        assertEquals(applicationBasicInfo.getAccessUrl(), expectedUrl,
                "Access URL should be trimmed to remove leading and trailing whitespaces");
    }

    /**
     * Test trimming of access URL returns null when null is set.
     */
    @Test(description = "Test access URL returns null when null is set")
    public void testAccessUrlNullHandling() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        applicationBasicInfo.setAccessUrl(null);
        assertNull(applicationBasicInfo.getAccessUrl(),
                "Access URL should return null when null is set");
    }

    /**
     * Test trimming of access URL with only whitespaces.
     */
    @Test(description = "Test access URL with only whitespaces")
    public void testAccessUrlOnlyWhitespaces() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithOnlyWhitespaces = "   ";

        applicationBasicInfo.setAccessUrl(urlWithOnlyWhitespaces);
        assertEquals(applicationBasicInfo.getAccessUrl(), "",
                "Access URL with only whitespaces should be trimmed to empty string");
    }

    /**
     * Test trimming of access URL with no whitespaces.
     */
    @Test(description = "Test access URL with no whitespaces")
    public void testAccessUrlNoWhitespaces() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithoutWhitespace = "https://example.com/app";

        applicationBasicInfo.setAccessUrl(urlWithoutWhitespace);
        assertEquals(applicationBasicInfo.getAccessUrl(), urlWithoutWhitespace,
                "Access URL without whitespaces should remain unchanged");
    }

    /**
     * Test trimming of access URL with leading whitespaces only.
     */
    @Test(description = "Test access URL with leading whitespaces only")
    public void testAccessUrlLeadingWhitespacesOnly() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithLeadingWhitespace = "   https://example.com/app";
        String expectedUrl = "https://example.com/app";

        applicationBasicInfo.setAccessUrl(urlWithLeadingWhitespace);
        assertEquals(applicationBasicInfo.getAccessUrl(), expectedUrl,
                "Access URL with leading whitespaces should be trimmed");
    }

    /**
     * Test trimming of access URL with trailing whitespaces only.
     */
    @Test(description = "Test access URL with trailing whitespaces only")
    public void testAccessUrlTrailingWhitespacesOnly() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithTrailingWhitespace = "https://example.com/app   ";
        String expectedUrl = "https://example.com/app";

        applicationBasicInfo.setAccessUrl(urlWithTrailingWhitespace);
        assertEquals(applicationBasicInfo.getAccessUrl(), expectedUrl,
                "Access URL with trailing whitespaces should be trimmed");
    }

    /**
     * Test trimming of access URL with tabs and newlines.
     */
    @Test(description = "Test access URL with tabs and newlines")
    public void testAccessUrlWithTabsAndNewlines() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String urlWithTabsAndNewlines = "\t\nhttps://example.com/app\n\t";
        String expectedUrl = "https://example.com/app";

        applicationBasicInfo.setAccessUrl(urlWithTabsAndNewlines);
        assertEquals(applicationBasicInfo.getAccessUrl(), expectedUrl,
                "Access URL with tabs and newlines should be trimmed");
    }

    /**
     * Test trimming of access URL with empty string.
     */
    @Test(description = "Test access URL with empty string")
    public void testAccessUrlEmptyString() {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        String emptyUrl = "";

        applicationBasicInfo.setAccessUrl(emptyUrl);
        assertEquals(applicationBasicInfo.getAccessUrl(), "",
                "Empty access URL should remain empty");
    }
}


