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
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Converts the properties of the template object to list of Attributes to pass to Configuration Management service.
 */
public class TemplatePropertiesToAttributes implements Function<Template, List<Attribute>> {

    @Override
    public List<Attribute> apply(Template template) {

        List<Attribute> attributeList = new ArrayList<>();

        attributeList.add(new Attribute(TemplateMgtConstants.TemplateAttributes.TEMPLATE_DESCRIPTION, template
                .getDescription()));
        attributeList.add(new Attribute(TemplateMgtConstants.TemplateAttributes.TEMPLATE_IMAGE_URL, template
                .getImageUrl()));
        if (!template.getPropertiesMap().isEmpty()) {
            template.getPropertiesMap().forEach((key, value) -> {
                Attribute attribute = new Attribute(key, value);
                attributeList.add(attribute);
            });
        }
        return attributeList;
    }
}
