package org.wso2.carbon.identity.template.mgt.endpoint;

import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateDTO;

import javax.ws.rs.core.Response;

public abstract class TemplatesApiService {

    public abstract Response addTemplate(TemplateDTO template);

    public abstract Response deleteTemplate(String templateName);

    public abstract Response getTemplateByName(String templateName);

    public abstract Response getTemplates(Integer limit, Integer offset);

    public abstract Response updateTemplate(String templateName, TemplateDTO newTemplate);
}

