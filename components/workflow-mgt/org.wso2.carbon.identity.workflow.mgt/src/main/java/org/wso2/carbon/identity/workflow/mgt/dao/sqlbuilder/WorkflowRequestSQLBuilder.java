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

package org.wso2.carbon.identity.workflow.mgt.dao.sqlbuilder;

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.user.core.model.SqlBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL Builder for Workflow Request operations.
 * This class builds SQL queries for various operations related to workflow
 * requests.
 */
public class WorkflowRequestSQLBuilder extends SqlBuilder {

    private static final String DEFAULT_ORDER_BY = " ORDER BY CREATED_AT DESC";
    private static final String ALL_FILTER = "ALL";
    private static final String ORDER_BY_STRING = " ORDER BY ";

    private Integer limit;
    private Integer offset;
    private final String databaseType;
    private boolean isSkipOrderingAndPagination;

    public WorkflowRequestSQLBuilder(String databaseType, String baseQuery) {

        super(new StringBuilder(baseQuery));
        if (databaseType == null || databaseType.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        this.databaseType = databaseType.toUpperCase();
    }

    public WorkflowRequestSQLBuilder setLimit(Integer limit) {

        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        this.limit = limit;
        return this;
    }

    public WorkflowRequestSQLBuilder setOffset(Integer offset) {

        if (offset != null && offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
        this.offset = offset;
        return this;
    }

    public WorkflowRequestSQLBuilder filterByTenantId(int tenantId) {

        super.where("TENANT_ID = ?", tenantId);
        return this;
    }

    public WorkflowRequestSQLBuilder filterByStatus(String status) {

        if (isValidFilter(status)) {
            super.where("STATUS = ?", status);
        }
        return this;
    }

    public WorkflowRequestSQLBuilder filterByOperationType(String operationType) {

        if (isValidFilter(operationType)) {
            super.where("OPERATION_TYPE = ?", operationType);
        }
        return this;
    }

    public WorkflowRequestSQLBuilder filterByCreatedBy(String createdBy) {

        if (isValidFilter(createdBy)) {
            super.where("CREATED_BY = ?", createdBy);
        }
        return this;
    }

    public WorkflowRequestSQLBuilder filterByCreatedDateRange(Timestamp startDate, Timestamp endDate) {

        if (startDate != null) {
            super.where("CREATED_AT >= ?", startDate);
        }
        if (endDate != null) {
            super.where("CREATED_AT <= ?", endDate);
        }
        return this;
    }

    public WorkflowRequestSQLBuilder filterByUpdatedDateRange(Timestamp startDate, Timestamp endDate) {

        if (startDate != null) {
            super.where("UPDATED_AT >= ?", startDate);
        }
        if (endDate != null) {
            super.where("UPDATED_AT <= ?", endDate);
        }
        return this;
    }

    @Override
    public String getQuery() {

        String query = super.getQuery();

        if (!isSkipOrderingAndPagination) {
            if (!query.contains(ORDER_BY_STRING)) {
                query += DEFAULT_ORDER_BY;
            }

            if (limit != null) {
                query = applyDBSpecificPagination(query);
            }
        }

        return query;
    }

    private String applyDBSpecificPagination(String query) {

        switch (databaseType) {
            case "ORACLE":
                if (offset != null && offset > 0) {
                    return "SELECT * FROM (" +
                            "SELECT a.*, ROWNUM rnum FROM (" + query + ") a " +
                            "WHERE ROWNUM <= ?" +
                            ") WHERE rnum > ?";
                } else {
                    return "SELECT * FROM (" + query + ") WHERE ROWNUM <= ?";
                }
            case "MYSQL":
            case "POSTGRESQL":
            case "H2":
                if (offset != null) {
                    return query + " LIMIT ? OFFSET ?";
                } else {
                    return query + " LIMIT ?";
                }
            case "DB2":
            case "DB2SQL":
                if (offset != null) {
                    return query + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
                } else {
                    return query + " FETCH FIRST ? ROWS ONLY";
                }
            case "MSSQL":
                if (offset != null) {
                    return query + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
                } else {
                    return "SELECT TOP (?) * FROM (" + query + ") AS result";
                }
            case "INFORMIX":
                if (offset != null) {
                    return "SELECT SKIP ? FIRST ? * FROM (" + query + ") AS result";
                } else {
                    return "SELECT FIRST ? * FROM (" + query + ") AS result";
                }
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType);
        }
    }

    public List<WorkflowRequest> execute() throws InternalWorkflowException {

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<WorkflowRequest> requests = new ArrayList<>();

        try {
            connection = IdentityDatabaseUtil.getDBConnection(false);
            String finalQuery = getQuery();
            stmt = connection.prepareStatement(finalQuery);

            List<Object> orderedParams = super.getOrderedParameters();
            int paramIndex = 1;

            for (Object param : orderedParams) {
                if (param instanceof String) {
                    stmt.setString(paramIndex, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(paramIndex, (Integer) param);
                } else if (param instanceof Long) {
                    stmt.setLong(paramIndex, (Long) param);
                } else if (param instanceof java.sql.Timestamp) {
                    stmt.setTimestamp(paramIndex, (java.sql.Timestamp) param);
                }
                paramIndex++;
            }

            setPaginationParams(stmt, paramIndex);

            rs = stmt.executeQuery();

            while (rs.next()) {
                WorkflowRequest request = mapResultSetToWorkflowRequest(rs);
                requests.add(request);
            }

            return requests;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error while executing workflow request query: " + e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, stmt);
        }
    }

    public int executeCount() throws InternalWorkflowException {

        isSkipOrderingAndPagination = true;
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            connection = IdentityDatabaseUtil.getDBConnection(false);
            String finalQuery = getQuery();
            stmt = connection.prepareStatement(finalQuery);

            List<Object> orderedParams = super.getOrderedParameters();
            int paramIndex = 1;

            for (Object param : orderedParams) {
                if (param instanceof String) {
                    stmt.setString(paramIndex, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(paramIndex, (Integer) param);
                } else if (param instanceof Long) {
                    stmt.setLong(paramIndex, (Long) param);
                } else if (param instanceof java.sql.Timestamp) {
                    stmt.setTimestamp(paramIndex, (java.sql.Timestamp) param);
                }
                paramIndex++;
            }

            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

            return count;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error while executing workflow request query: " + e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, stmt);
        }
    }

    private void setPaginationParams(PreparedStatement stmt, int startIndex) throws SQLException {

        if (limit == null) {
            return;
        }

        int paramIndex = startIndex;

        switch (databaseType) {
            case "ORACLE":
                if (offset != null && offset > 0) {
                    stmt.setInt(paramIndex++, offset + limit);
                    stmt.setInt(paramIndex, offset);
                } else {
                    stmt.setInt(paramIndex, limit);
                }
                break;
            case "DB2":
            case "DB2SQL":
            case "MSSQL":
            case "INFORMIX":
                if (offset != null) {
                    stmt.setInt(paramIndex++, offset);
                    stmt.setInt(paramIndex, limit);
                } else {
                    stmt.setInt(paramIndex, limit);
                }
                break;
            default:
                if (offset != null) {
                    stmt.setInt(paramIndex++, limit);
                    stmt.setInt(paramIndex, offset);
                } else {
                    stmt.setInt(paramIndex, limit);
                }
                break;
        }
    }

    private WorkflowRequest mapResultSetToWorkflowRequest(ResultSet rs) throws SQLException {

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestId(rs.getString("UUID"));
        request.setCreatedBy(rs.getString("CREATED_BY"));
        request.setCreatedAt(rs.getTimestamp("CREATED_AT").toInstant().toString());
        request.setUpdatedAt(rs.getTimestamp("UPDATED_AT").toInstant().toString());
        request.setStatus(rs.getString("STATUS"));
        request.setOperationType(rs.getString("OPERATION_TYPE"));
        try {
            request.setRequestParams(getBlobValue(rs.getBinaryStream("REQUEST")));
            return request;
        } catch (IOException e) {
            throw new SQLException("Error while retrieving request parameters from the database", e);
        }
    }

    private boolean isValidFilter(String value) {

        return value != null && !value.trim().isEmpty() && !ALL_FILTER.equalsIgnoreCase(value);
    }

    public WorkflowRequestSQLBuilder getAllRequestsWithSpecificFilters(int tenantId, String createdBy,
            String operationType, String status, String category, String startDate, String endDate, int limit,
                                                                       int offset) {

        WorkflowRequestSQLBuilder builder = this.filterByTenantId(tenantId);

        if ("CREATED".equalsIgnoreCase(category)) {
            if (startDate != null && endDate != null) {
                builder = builder.filterByCreatedDateRange(Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
            }
        } else if ("UPDATED".equalsIgnoreCase(category)) {
            if (startDate != null && endDate != null) {
                builder = builder.filterByUpdatedDateRange(Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
            }
        }

        if (createdBy != null && !createdBy.isEmpty()) {
            builder = builder.filterByCreatedBy(createdBy);
        }

        if (operationType != null && !operationType.isEmpty()) {
            builder = builder.filterByOperationType(operationType);
        }

        if (status != null && !status.isEmpty()) {
            builder = builder.filterByStatus(status);
        }

        builder = builder.setLimit(limit).setOffset(offset);
        return builder;
    }

    /**
     * Retrieves the string content from the given input stream of a blob.
     *
     * @param is the input stream to read the blob data from.
     * @return the string content read from the input stream.
     * @throws IOException if an I/O error occurs while reading from the input stream or closing the reader.
     */
    private String getBlobValue(InputStream is) throws IOException {

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        }
        return null;
    }
}
