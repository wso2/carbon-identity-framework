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
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;

import java.util.List;
import java.util.Map;

/**
 * Utility class for Action Management DAO.
 */
public class ActionManagementDAOUtil {

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
            throw new ActionMgtServerException(dbPropertyToRead + " does not exist in the DB.");
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
     * Builds an updated Action DTO by merging the updated endpoint configuration with the existing action.
     *
     * @param updatingActionDTO Updating Action DTO.
     * @param existingActionDTO Existing Action DTO.
     * @return Resolved Action DTO with the updated endpoint config.
     */
    public ActionDTO buildActionDTOWithEndpoint(ActionDTO updatingActionDTO, ActionDTO existingActionDTO) {

        ActionDTOBuilder builder = new ActionDTOBuilder(existingActionDTO);
        EndpointConfig.EndpointConfigBuilder endpointBuilder =
                new EndpointConfig.EndpointConfigBuilder(existingActionDTO.getEndpoint());

        EndpointConfig updatingEndpoint = updatingActionDTO.getEndpoint();
        if (updatingEndpoint.getUri() != null) {
            endpointBuilder.uri(updatingEndpoint.getUri());
        }
        if (updatingEndpoint.getAllowedHeaders() != null) {
            endpointBuilder.allowedHeaders(updatingEndpoint.getAllowedHeaders());
        }
        if (updatingEndpoint.getAllowedParameters() != null) {
            endpointBuilder.allowedParameters(updatingEndpoint.getAllowedParameters());
        }

        // If endpoint authentication is updated.
        Authentication updatingAuthentication = updatingEndpoint.getAuthentication();
        if (updatingAuthentication != null) {
            endpointBuilder = endpointBuilder.authentication(updatingAuthentication);
        }

        builder = builder.endpoint(endpointBuilder.build());
        return builder.build();
    }
}
