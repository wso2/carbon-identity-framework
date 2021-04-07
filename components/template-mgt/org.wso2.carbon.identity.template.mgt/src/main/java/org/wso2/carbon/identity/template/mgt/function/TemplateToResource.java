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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.generateUniqueID;

/**
 * Converts the Template object to Resource object tp pass to configuration manager service.
 */
public class TemplateToResource implements Function<Template, Resource> {

    @Override
    public Resource apply(Template template) {

        List<ResourceFile> resourceFileList = new ArrayList<>();
        resourceFileList.add(getResourceFile(template));

        Resource resource = new Resource();
        resource.setResourceName(template.getTemplateName());
        resource.setResourceType(getResourceTypeFromTemplateType(template.getTemplateType()));
        resource.setAttributes(new TemplatePropertiesToAttributes().apply(template));
        resource.setResourceId(template.getTemplateId());
        resource.setTenantDomain(TemplateMgtUtils.getTenantDomainFromCarbonContext());
        resource.setFiles(resourceFileList);
        return resource;
    }

    private String getResourceTypeFromTemplateType(TemplateMgtConstants.TemplateType templateType) {

        if (templateType != null) {
            return templateType.toString();
        } else {
            return null;
        }
    }

    private ResourceFile getResourceFile(Template template) {

        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setId(generateUniqueID());
        resourceFile.setName(template.getTemplateType().toString() + "_template_object");
        resourceFile.setInputStream(IOUtils.toInputStream(template.getTemplateScript()));
        return resourceFile;
    }
}
