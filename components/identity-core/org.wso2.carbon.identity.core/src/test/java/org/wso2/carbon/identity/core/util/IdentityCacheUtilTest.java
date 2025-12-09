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

package org.wso2.carbon.identity.core.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.cache.CacheEntry;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for IdentityCacheUtil.
 */
public class IdentityCacheUtilTest {

    /**
     * Data provider for cache entry expiration test cases.
     *
     * @return Test data containing validity period and expected expiration result.
     */
    @DataProvider(name = "cacheExpirationData")
    public Object[][] cacheExpirationData() {

        long futureTime = System.nanoTime() + (60L * 60L * 1000000000L);
        long pastTime = System.nanoTime() - (60L * 60L * 1000000000L);

        return new Object[][] {
                // validity period, expected expiration result, description.
                {null, true, "Null cache entry should be considered expired"},
                {0L, false, "Cache entry with validity period 0 should never expire"},
                {futureTime, false, "Cache entry with future validity period should not be expired"},
                {pastTime, true, "Cache entry with past validity period should be expired"}
        };
    }

    /**
     * Test cache entry expiration with various validity periods.
     *
     * @param validityPeriod The validity period in nanoseconds.
     * @param expectedExpired The expected expiration result.
     * @param description Description of the test case.
     */
    @Test(dataProvider = "cacheExpirationData")
    public void testIsCacheEntryExpired(Long validityPeriod, boolean expectedExpired, String description) {

        CacheEntry cacheEntry = validityPeriod == null ? null : new TestCacheEntry(validityPeriod);
        assertEquals(IdentityCacheUtil.isCacheEntryExpired(cacheEntry), expectedExpired, description);
    }

    /**
     * Test implementation of CacheEntry for testing purposes.
     */
    private static class TestCacheEntry extends CacheEntry {

        private static final long serialVersionUID = 1L;
        private final long validityPeriod;

        public TestCacheEntry(long validityPeriod) {
            this.validityPeriod = validityPeriod;
        }

        @Override
        public long getValidityPeriod() {
            return validityPeriod;
        }
    }
}
