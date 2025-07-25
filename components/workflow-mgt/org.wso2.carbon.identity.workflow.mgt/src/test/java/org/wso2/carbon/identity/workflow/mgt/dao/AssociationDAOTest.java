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

package org.wso2.carbon.identity.workflow.mgt.dao;

import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

public class AssociationDAOTest {

    @Test
    public void testGetAssociationByInvalidAssociationId() {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                    .thenReturn(mockConnection);
            AssociationDAO dao = new AssociationDAO();
            String invalidAssociationId = "invalid_id";
            assertThrows(WorkflowClientException.class, () -> dao.getAssociation(invalidAssociationId));
        }
    }

    @Test
    public void testUpdateAssociationByInvalidAssociationId() throws SQLException {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDBConnection).thenReturn(mockConnection);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            AssociationDAO dao = new AssociationDAO();
            Association associationDTO = new Association();
            associationDTO.setEventId("ADD_USER");
            associationDTO.setAssociationName("User Addition Association");
            associationDTO.setCondition("true");
            associationDTO.setWorkflowId("12345");
            associationDTO.setAssociationId("invalid_id");
            assertThrows(WorkflowClientException.class, () -> dao.updateAssociation(associationDTO));
        }
    }
}
