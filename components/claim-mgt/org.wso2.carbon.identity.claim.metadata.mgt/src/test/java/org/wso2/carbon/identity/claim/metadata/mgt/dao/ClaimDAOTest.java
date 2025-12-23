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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.util.SQLConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;

public class ClaimDAOTest {

    private final int testTenantId = -1234;
    private final int testClaimId = 11;
    @Mock
    Connection mockedConnection;
    @Mock
    PreparedStatement mockedPreparedStatement;
    @Mock
    ResultSet mockedResultSet;
    private ClaimDAO claimDAO;

    @BeforeClass
    public void setup() throws SQLException {

        openMocks(this);
        claimDAO = new ClaimDAO();

        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
    }

    @Test(dataProvider = "claimPropertiesProvider")
    public void testGetClaimProperties(String[] propertyNames, String[] propertyValues, int expectedSize)
            throws ClaimMetadataException, SQLException {

        // Simulate result set.
        when(mockedResultSet.next())
                .thenReturn(true, true, true, false); // 3 rows, then end.

        when(mockedResultSet.getString(SQLConstants.PROPERTY_NAME_COLUMN))
                .thenReturn(propertyNames[0], propertyNames[1], propertyNames[2]);

        when(mockedResultSet.getString(SQLConstants.PROPERTY_VALUE_COLUMN))
                .thenReturn(propertyValues[0], propertyValues[1], propertyValues[2]);

        Map<String, String> claims = claimDAO.getClaimProperties(mockedConnection, testClaimId, testTenantId);
        assertEquals(claims.size(), expectedSize, "Unexpected number of properties in the map.");
    }

    @Test(dataProvider = "claimPropertyNamesProvider")
    public void testAddClaimProperties(Map<String, String> claimProperties) throws ClaimMetadataException {

        claimDAO.addClaimProperties(mockedConnection, testClaimId, claimProperties, testTenantId);

    }

    @DataProvider
    public Object[][] claimPropertyNamesProvider() {

        Map<String, String> claimProperties1 = new HashMap<>();
        claimProperties1.put("DisplayName", "value1");
        claimProperties1.put("subAttributes",
                "http://wso2.org/claims/subattribute1 http://wso2.org/claims/subattribute2");

        Map<String, String> claimProperties2 = new HashMap<>();
        claimProperties2.put("Description", "desc");
        claimProperties2.put("DisplayName", "value1");
        return new Object[][]{
                {claimProperties1},
                {claimProperties2}
        };
    }

    @DataProvider(name = "claimPropertiesProvider")
    public Object[][] claimPropertiesProvider() {

        return new Object[][]{
                {
                        new String[]{"DisplayName", "subAttribute.1", "subAttribute.2"},
                        new String[]{"value1", "http://wso2.org/claims/subattribute1",
                                "http://wso2.org/claims/subattribute2"},
                        2
                },
                {
                        new String[]{"DisplayName", "canonicalValue.1", "canonicalValue.2"},
                        new String[]{"value1", "http://wso2.org/claims/canonicalValue1",
                                "http://wso2.org/claims/canonicalValue2"},
                        2
                },
                {
                        new String[]{"subAttribute.1", "canonicalValue.1", "canonicalValue.2"},
                        new String[]{"http://wso2.org/claims/subattribute1",
                                "http://wso2.org/claims/canonicalValue1", "http://wso2.org/claims/canonicalValue2"},
                        2
                },
                {
                        new String[]{"DisplayName", "Description", "dataType"},
                        new String[]{"value1", "testDescription", "complex"},
                        3
                }
        };
    }

}
