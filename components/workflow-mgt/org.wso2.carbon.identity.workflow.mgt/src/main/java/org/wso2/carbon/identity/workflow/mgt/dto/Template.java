/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.dto;

import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParametersMetaData;

/**
 * Template class is a DTO that is represent basic Template data and the
 * template specific MetaData
 *
 */
public class Template {

    private String templateId;
    private String name;
    private String description;

    private ParametersMetaData parametersMetaData;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParametersMetaData getParametersMetaData() {
        return parametersMetaData;
    }

    public void setParametersMetaData(ParametersMetaData parametersMetaData) {
        this.parametersMetaData = parametersMetaData;
    }


}
