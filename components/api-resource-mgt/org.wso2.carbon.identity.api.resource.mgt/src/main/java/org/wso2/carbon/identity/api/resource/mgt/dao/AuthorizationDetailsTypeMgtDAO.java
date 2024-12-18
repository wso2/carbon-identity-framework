/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.dao;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for managing Authorization Detail Types.
 *
 * <p>This interface defines the methods required for interacting with the underlying
 * persistence layer to manage authorization detail types associated with APIs in a
 * multi-tenant environment. It includes methods to perform CRUD operations on
 * authorization detail types while handling tenant-specific data.</p>
 */
public interface AuthorizationDetailsTypeMgtDAO {

    /**
     * Adds a list of authorization detail types for a specific API.
     *
     * @param apiId                     The API identifier.
     * @param authorizationDetailsTypes The list of {@link AuthorizationDetailsType} objects to add.
     * @param tenantId                  The tenant identifier.
     * @return A list of persisted {@link AuthorizationDetailsType} instances.
     * @throws APIResourceMgtException If an error occurs while adding the authorization detail types.
     */
    List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Adds a list of authorization detail types for a specific API using an existing database connection.
     *
     * @param connection                The existing database connection.
     * @param apiId                     The API identifier.
     * @param authorizationDetailsTypes The list of {@link AuthorizationDetailsType} objects to add.
     * @param tenantId                  The tenant identifier.
     * @return A list of persisted {@link AuthorizationDetailsType} instances.
     * @throws SQLException            If an error occurs while adding the authorization detail types.
     * @throws APIResourceMgtException If provided authorization details type already exists in the database.
     */
    List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            Connection connection, String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
            Integer tenantId) throws SQLException, APIResourceMgtException;

    /**
     * Deletes a specific authorization detail type for a given API and type.
     *
     * @param apiId    The API identifier.
     * @param type     The unique type of the authorization detail.
     * @param tenantId The tenant identifier.
     * @throws APIResourceMgtException If an error occurs while deleting the authorization detail type.
     */
    void deleteAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Deletes a specific authorization detail type for a given API and type.
     *
     * @param apiId    The API identifier.
     * @param typeId   The authorization details type identifier.
     * @param tenantId The tenant identifier.
     * @throws APIResourceMgtException If an error occurs while deleting the authorization detail type.
     */
    void deleteAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Deletes all authorization detail types associated with a specific API.
     *
     * @param apiId    The API identifier.
     * @param tenantId The tenant identifier.
     * @throws APIResourceMgtException If an error occurs while deleting the authorization detail types.
     */
    void deleteAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId) throws APIResourceMgtException;

    /**
     * Deletes all authorization detail types associated with a specific API.
     *
     * @param connection The existing database connection.
     * @param apiId      The API identifier.
     * @param tenantId   The tenant identifier.
     * @throws SQLException If an error occurs while deleting the authorization detail types.
     */
    void deleteAuthorizationDetailsTypesByApiId(Connection connection, String apiId, Integer tenantId)
            throws SQLException;

    /**
     * Retrieves a specific authorization detail type for an API by its type ID.
     *
     * @param apiId    The API identifier.
     * @param type     The type of the authorization detail.
     * @param tenantId The tenant identifier.
     * @return The {@link AuthorizationDetailsType} object matching the API ID and type.
     * @throws APIResourceMgtException If an error occurs while retrieving the authorization detail type.
     */
    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndType(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Retrieves a specific authorization detail type for an API by its type ID.
     *
     * @param apiId    The API identifier.
     * @param typeId   The authorization details type identifier.
     * @param tenantId The tenant identifier.
     * @return The {@link AuthorizationDetailsType} object matching the API ID and type.
     * @throws APIResourceMgtException If an error occurs while retrieving the authorization detail type.
     */
    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Retrieves a list of authorization detail types that match the given expression nodes.
     *
     * @param expressionNodes The list of expression nodes used for filtering the authorization detail types.
     * @param tenantId        The tenant identifier.
     * @return A list of {@link AuthorizationDetailsType} objects matching the filter criteria.
     * @throws APIResourceMgtException If an error occurs while retrieving authorization detail types.
     */
    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(List<ExpressionNode> expressionNodes, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Retrieves a list of authorization detail types for a specific API ID.
     *
     * @param apiId    The API identifier.
     * @param tenantId The tenant identifier.
     * @return A list of {@link AuthorizationDetailsType} objects associated with the specified API.
     * @throws APIResourceMgtException If an error occurs while retrieving authorization detail types by API ID.
     */
    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Retrieves a list of authorization detail types for a specific API ID.
     *
     * @param connection The existing database connection.
     * @param apiId      The API identifier.
     * @param tenantId   The tenant identifier.
     * @return A list of {@link AuthorizationDetailsType} objects associated with the specified API.
     * @throws SQLException If an error occurs while retrieving authorization detail types by API ID.
     */
    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(Connection connection, String apiId,
                                                                       Integer tenantId) throws SQLException;

    /**
     * Checks if an authorization detail type exists for a given API and type.
     *
     * @param apiId    The API identifier.
     * @param type     The type of authorization detail.
     * @param tenantId The tenant identifier.
     * @return {@code true} if the authorization detail type exists, otherwise {@code false}.
     * @throws APIResourceMgtException If an error occurs while checking the existence of the authorization detail type.
     */
    boolean isAuthorizationDetailsTypeExists(String apiId, String type, Integer tenantId)
            throws APIResourceMgtException;

    /**
     * Updates a list of authorization detail types for a specific API.
     *
     * @param apiId                     The API identifier.
     * @param authorizationDetailsTypes The list of {@link AuthorizationDetailsType} objects to update.
     * @param tenantId                  The tenant identifier.
     * @throws APIResourceMgtException If an error occurs while updating the authorization detail types.
     */
    void updateAuthorizationDetailsTypes(String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes,
                                         Integer tenantId) throws APIResourceMgtException;

    /**
     * Updates a list of authorization detail types for a specific API using an existing database connection.
     *
     * @param connection                The existing database connection.
     * @param apiId                     The API identifier.
     * @param authorizationDetailsTypes The list of {@link AuthorizationDetailsType} objects to update.
     * @param tenantId                  The tenant identifier.
     * @throws SQLException            If an error occurs while updating the authorization detail types.
     * @throws APIResourceMgtException If provided authorization details type already exists in the database.
     */
    void updateAuthorizationDetailsTypes(Connection connection, String apiId,
                                         List<AuthorizationDetailsType> authorizationDetailsTypes, Integer tenantId)
            throws SQLException, APIResourceMgtException;

    /**
     * Replaces a list of authorization detail types by removing the specified types and adding new ones
     * for a specific API in the tenant.
     *
     * @param apiId                            The API identifier.
     * @param removedAuthorizationDetailsTypes The list of authorization detail types to be removed.
     * @param addedAuthorizationDetailsTypes   The list of new authorization detail types to be added.
     * @param tenantId                         The tenant identifier.
     * @throws APIResourceMgtException If an error occurs during the replace operation.
     */
    void updateAuthorizationDetailsTypes(String apiId, List<String> removedAuthorizationDetailsTypes,
                                         List<AuthorizationDetailsType> addedAuthorizationDetailsTypes,
                                         Integer tenantId) throws APIResourceMgtException;
}
