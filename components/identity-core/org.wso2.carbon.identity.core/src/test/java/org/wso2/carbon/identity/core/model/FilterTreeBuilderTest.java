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

package org.wso2.carbon.identity.core.model;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class FilterTreeBuilderTest {

    @DataProvider(name = "filterData")
    public Object[][] getFilterData() {

        return new Object[][]{
            // Simple filters
            {"Simple equal filter", "userName eq john", new String[]{"userName eq john"}},
            {"Filter with spaces", "userName eq John Doe", new String[]{"userName eq John Doe"}},
            {"AND as value", "name eq and", new String[]{"name eq and"}},
            {"OR as value", "name sw or", new String[]{"name sw or"}},
            {"NOT as value", "name co not", new String[]{"name co not"}},
            {"Presence filter", "userName pr", new String[]{"userName pr"}},
            {"Filter with quotes", "firstName eq 'John name'", new String[]{"firstName eq John name"}},
            
            // Complex filters
            {"Complex AND filter",
                "userName eq john and lastName eq sherif",
                new String[]{
                    "userName eq john",
                    IdentityCoreConstants.Filter.AND,
                    "lastName eq sherif"
                }
            },
            {"Complex OR filter", 
                "userName eq john or email eq john@example.com",
                new String[]{
                    "userName eq john",
                    IdentityCoreConstants.Filter.OR,
                    "email eq john@example.com"
                }
            },
            {"Operator word as value and operator", 
                "userName ew or and lastName sw and",
                new String[]{
                    "userName ew or",
                    IdentityCoreConstants.Filter.AND,
                    "lastName sw and"
                }
            },
            {"Presence with AND filter",
                "userName pr and lastName eq sherif",
                new String[]{
                    "userName pr",
                    "and",
                    "lastName eq sherif"
                }
            },
            
            // Parentheses filter
            {"Filter with parentheses", 
                "not (userName eq john)",
                new String[]{
                    IdentityCoreConstants.Filter.NOT,
                    "(",
                    "userName eq john",
                    ")"
                }
            }
        };
    }

    @Test(dataProvider = "filterData")
    public void testFilter(String testName, String filter, String[] expectedTokens)
            throws IOException, NoSuchFieldException, IllegalAccessException {

        FilterTreeBuilder builder = new FilterTreeBuilder(filter);
        List<String> tokenList = getTokenList(builder);

        Assert.assertEquals(tokenList.size(), expectedTokens.length,
                String.format("Token list size mismatch for test: %s", testName));
        for (int i = 0; i < expectedTokens.length; i++) {
            Assert.assertEquals(tokenList.get(i), expectedTokens[i],
                    String.format("Token mismatch at position %d for test: %s", i, testName));
        }
    }

    /**
     * Helper method to access the private tokenList field
     */
    @SuppressWarnings("unchecked")
    private List<String> getTokenList(FilterTreeBuilder builder) throws NoSuchFieldException, IllegalAccessException {

        Field tokenListField = FilterTreeBuilder.class.getDeclaredField("tokenList");
        tokenListField.setAccessible(true);
        return (List<String>) tokenListField.get(builder);
    }
}
