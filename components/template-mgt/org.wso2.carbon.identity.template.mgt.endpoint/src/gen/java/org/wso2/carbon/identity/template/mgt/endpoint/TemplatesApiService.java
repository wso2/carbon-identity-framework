package org.wso2.carbon.identity.template.mgt.endpoint;

import org.wso2.carbon.identity.template.mgt.endpoint.*;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.*;

import org.wso2.carbon.identity.template.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.AddTemplateResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.GetTemplatesResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.UpdateTemplateRequestDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.UpdateSuccessResponseDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class TemplatesApiService {
    public abstract Response addTemplate(TemplateRequestDTO template);
    public abstract Response deleteTemplate(String templateName);
    public abstract Response getTemplateByName(String templateName);
    public abstract Response getTemplates(Integer limit,Integer offset);
    public abstract Response updateTemplate(String templateName,UpdateTemplateRequestDTO newTemplate);
}

