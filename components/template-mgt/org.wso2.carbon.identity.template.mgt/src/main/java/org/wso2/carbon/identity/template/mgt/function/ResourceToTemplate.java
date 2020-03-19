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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.function;

import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class ResourceToTemplate implements Function<Resource, Template> {

    @Override
    public Template apply(Resource resource) {

        Template template = convertAttributesToPropeties(resource.getAttributes());
        template.setTemplateName(resource.getResourceName());
        template.setTemplateId(resource.getResourceId());
        template.setTenantId(IdentityTenantUtil.getTenantId(resource.getTenantDomain()));
        return template;
    }

    private Template convertAttributesToPropeties(List<Attribute> attributeList) {

        Template template = new Template();
        Map<String, String> propertiesMap = new HashMap<>();

        attributeList.forEach(attribute -> {
            switch (attribute.getKey()) {
                case TemplateMgtConstants.TemplateAttributes.TEMPLATE_TYPE:
                    template.setTemplateType(TemplateMgtConstants.TemplateType.valueOf(attribute.getValue()));
                    break;
                case TemplateMgtConstants.TemplateAttributes.TEMPLATE_DESCRIPTION:
                    template.setDescription(attribute.getValue());
                    break;
                case TemplateMgtConstants.TemplateAttributes.TEMPLATE_IMAGE_URL:
                    template.setImageUrl(attribute.getValue());
                    break;
                default:
                    propertiesMap.put(attribute.getKey(), attribute.getValue());
            }
        });
        template.setPropertiesMap(propertiesMap);
        return template;
    }
}
