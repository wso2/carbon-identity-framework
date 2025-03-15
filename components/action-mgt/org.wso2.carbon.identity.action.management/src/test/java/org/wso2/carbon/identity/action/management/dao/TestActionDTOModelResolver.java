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

package org.wso2.carbon.identity.action.management.dao;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionPropertyForDAO;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.certificate.management.model.Certificate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PASSWORD_SHARING_TYPE_PROPERTY_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_CERTIFICATE;

/**
 * Test implementation of {@link ActionDTOModelResolver}.
 */
public class TestActionDTOModelResolver implements ActionDTOModelResolver {

    @Override
    public Action.ActionTypes getSupportedActionType() {

        return Action.ActionTypes.PRE_UPDATE_PASSWORD;
    }

    @Override
    public ActionDTO resolveForAddOperation(ActionDTO actionDTO, String tenantDomain) {

        Map<String, ActionPropertyForDAO> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME, new ActionPropertyForDAO(actionDTO
                .getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME).toString()));
        properties.put(CERTIFICATE_PROPERTY_NAME, new ActionPropertyForDAO(TestUtil.CERTIFICATE_ID));

        return new ActionDTO.BuilderForData(actionDTO).properties(properties).build();
    }

    @Override
    public ActionDTO resolveForGetOperation(ActionDTO actionDTO, String tenantDomain) {

        Map<String, Object> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                ((ActionPropertyForDAO) actionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME)).getValue());
        if (actionDTO.getProperty(CERTIFICATE_PROPERTY_NAME) != null) {
            properties.put(CERTIFICATE_PROPERTY_NAME, new Certificate.Builder()
                    .id(((ActionPropertyForDAO) actionDTO.getProperty(CERTIFICATE_PROPERTY_NAME)).getValue().toString())
                    .name(CERTIFICATE_NAME)
                    .certificateContent(TEST_CERTIFICATE)
                    .build());
        }

        return new ActionDTO.Builder(actionDTO).properties(properties).build();
    }

    @Override
    public List<ActionDTO> resolveForGetOperation(List<ActionDTO> actionDTOList, String tenantDomain) {

        List<ActionDTO> resolvedActionDTOList = new ArrayList<>();
        for (ActionDTO actionDTO : actionDTOList) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                    ((ActionPropertyForDAO) actionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME))
                            .getValue().toString());
            properties.put(CERTIFICATE_PROPERTY_NAME, new Certificate.Builder()
                    .id(((ActionPropertyForDAO) actionDTO.getProperty(CERTIFICATE_PROPERTY_NAME)).getValue().toString())
                    .name(CERTIFICATE_NAME)
                    .certificateContent(TEST_CERTIFICATE)
                    .build());

            resolvedActionDTOList.add(new ActionDTO.Builder(actionDTO).properties(properties).build());
        }

        return resolvedActionDTOList;
    }

    @Override
    public ActionDTO resolveForUpdateOperation(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                               String tenantDomain) {

        Map<String, ActionPropertyForDAO> properties = new HashMap<>();
        properties.put(PASSWORD_SHARING_TYPE_PROPERTY_NAME,
                new ActionPropertyForDAO(updatingActionDTO.getProperty(PASSWORD_SHARING_TYPE_PROPERTY_NAME)
                        .toString()));
        if (updatingActionDTO.getProperty(CERTIFICATE_PROPERTY_NAME) != null &&
                !StringUtils.EMPTY.equals(updatingActionDTO.getProperty(
                        CERTIFICATE_PROPERTY_NAME).toString())) {
            properties.put(CERTIFICATE_PROPERTY_NAME, new ActionPropertyForDAO(TestUtil.CERTIFICATE_ID));
        }

        return new ActionDTO.BuilderForData(updatingActionDTO).properties(properties).build();
    }

    @Override
    public void resolveForDeleteOperation(ActionDTO deletingActionDTO, String tenantDomain) {

        // No need to resolve anything for delete operation since this is a test implementation.
    }
}
