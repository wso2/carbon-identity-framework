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
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;

import java.util.List;
import java.util.Map;

/**
 * Utility class for Action Management DAO.
 */
public class ActionManagementDAOUtil {

    /**
     * Read a list of allowed headers and parameters from the database based on the given property name.
     *
     * @param properties            A map of action properties retrieved from the database.
     * @param dbPropertyToRead     The name of the property to read from the database.
     * @return A list of allowed properties as strings.
     * @throws ActionMgtServerException If an error occurs while reading the property from the database.
     */
    public List<String> resolveListFromBinaryObject(Map<String, ActionProperty> properties,
                                                    String dbPropertyToRead) throws ActionMgtServerException {

        Object allowedHeaders = properties.remove(dbPropertyToRead).getValue();
        String allowedHeaderJson = ((BinaryObject) allowedHeaders).getJSONString();

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(allowedHeaderJson, new TypeReference<List<String>>() { });
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
}
