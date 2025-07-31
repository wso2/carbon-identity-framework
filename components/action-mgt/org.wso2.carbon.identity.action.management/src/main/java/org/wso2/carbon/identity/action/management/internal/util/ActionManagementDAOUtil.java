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

package org.wso2.carbon.identity.action.management.internal.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Action Management DAO.
 */
public class ActionManagementDAOUtil {

    private static final Log LOG = LogFactory.getLog(ActionManagementDAOUtil.class);

    /**
     * Reads a database property from the given map of properties and converts it into a list of strings.
     *
     * @param properties       Map of properties where the database property is stored.
     * @param dbPropertyToRead The key of the database property to read.
     * @return A list of strings representing the database property value.
     * @throws ActionMgtServerException If the property does not exist or cannot be processed.
     */
    public List<String> readDBListProperty(Map<String, ActionProperty> properties,
                                           String dbPropertyToRead) throws ActionMgtServerException {

        ActionProperty dbActionProperty = properties.remove(dbPropertyToRead);
        if (dbActionProperty == null) {
            LOG.debug(dbPropertyToRead + " property does not exist in the DB.");
            return Collections.emptyList();
        }

        Object dbValueBinary = dbActionProperty.getValue();
        String dbValueJSON = ((BinaryObject) dbValueBinary).getJSONString();

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(dbValueJSON, new TypeReference<List<String>>() { });
        } catch (JsonProcessingException e) {
            throw new ActionMgtServerException("Error while reading " + dbPropertyToRead);
        }
    }

    /**
     * Creates an ActionProperty object from a list of attributes by converting them into a JSON string.
     *
     * @param attributes List of attributes to be converted into an ActionProperty.
     * @return An ActionProperty object containing the JSON representation of the attributes.
     * @throws ActionMgtServerException If an error occurs while converting the attributes to a JSON string.
     */
    public ActionProperty buildActionPropertyFromList(List<String> attributes) throws ActionMgtServerException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BinaryObject attributesBinaryObject = BinaryObject.fromJsonString(objectMapper
                    .writeValueAsString(attributes));
            return new ActionProperty.BuilderForDAO(attributesBinaryObject).build();
        } catch (JsonProcessingException e) {
            throw new ActionMgtServerException("Failed to convert the list to JSON string.", e);
        }
    }

    /**
     * Builds an updated EndpointConfig by merging the updated endpoint configuration with the existing endpoint.
     *
     * @param updatingEndpoint  The EndpointConfig containing the updated values.
     * @param existingEndpoint  The existing EndpointConfig to be updated.
     * @return Resolved EndpointConfig with the merged configuration.
     */
    public EndpointConfig buildUpdatingEndpointConfig(EndpointConfig updatingEndpoint,
                                                      EndpointConfig existingEndpoint) {

        EndpointConfig.EndpointConfigBuilder builder =
                new EndpointConfig.EndpointConfigBuilder(existingEndpoint);

        if (updatingEndpoint.getUri() != null) {
            builder.uri(updatingEndpoint.getUri());
        }
        if (updatingEndpoint.getAllowedHeaders() != null) {
            builder.allowedHeaders(updatingEndpoint.getAllowedHeaders());
        }
        if (updatingEndpoint.getAllowedParameters() != null) {
            builder.allowedParameters(updatingEndpoint.getAllowedParameters());
        }

        // If endpoint authentication is updated.
        Authentication updatingAuthentication = updatingEndpoint.getAuthentication();
        if (updatingAuthentication != null) {
            builder = builder.authentication(updatingAuthentication);
        }

        return builder.build();
    }

    /**
     * Builds a map of ActionProperty objects from the given EndpointConfig.
     * This includes properties such as URI, authentication type, allowed headers, and allowed parameters.
     *
     * @param updatingEndpoint The EndpointConfig containing the updated endpoint properties.
     * @return A map of ActionProperty objects representing the updated endpoint properties.
     * @throws ActionMgtServerException If an error occurs while processing the endpoint properties.
     */
    public Map<String, ActionProperty> getUpdatingEndpointProperties(EndpointConfig updatingEndpoint)
            throws ActionMgtServerException {

        Map<String, ActionProperty> updatingEndpointProperties = new HashMap<>();

        updatingEndpointProperties.put(ActionMgtConstants.URI_PROPERTY, new ActionProperty.BuilderForDAO(
                updatingEndpoint.getUri()).build());
        updatingEndpointProperties.put(ActionMgtConstants.AUTHN_TYPE_PROPERTY, new ActionProperty.BuilderForDAO(
                updatingEndpoint.getAuthentication().getType().name()).build());

        updatingEndpoint.getAuthentication().getProperties().forEach(authProperty ->
                updatingEndpointProperties.put(authProperty.getName(),
                        new ActionProperty.BuilderForDAO(authProperty.getValue()).build()));
        // Allowed headers and parameters are optional properties.
        if (CollectionUtils.isNotEmpty(updatingEndpoint.getAllowedHeaders())) {
            updatingEndpointProperties.put(ActionMgtConstants.ALLOWED_HEADERS_PROPERTY,
                    buildActionPropertyFromList(updatingEndpoint.getAllowedHeaders()));
        }
        if (CollectionUtils.isNotEmpty(updatingEndpoint.getAllowedParameters())) {
            updatingEndpointProperties.put(ActionMgtConstants.ALLOWED_PARAMETERS_PROPERTY,
                    buildActionPropertyFromList(updatingEndpoint.getAllowedParameters()));
        }

        return updatingEndpointProperties;
    }
}
