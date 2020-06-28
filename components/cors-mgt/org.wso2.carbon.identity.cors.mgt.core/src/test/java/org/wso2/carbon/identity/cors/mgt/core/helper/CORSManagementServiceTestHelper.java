/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.helper;

import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.cors.mgt.core.constant.TestConstants.APP_ID_1;

/**
 * Helper class for CORSServiceTest.
 */
public class CORSManagementServiceTestHelper {

    public static ResourceAdd getSampleResourceAdd(String origin) {

        List<Attribute> attributeList = new ArrayList<>();
        Attribute attribute = new Attribute(APP_ID_1, "");
        attributeList.add(attribute);

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(origin);
        resourceAdd.setAttributes(attributeList);
        return resourceAdd;
    }
}
