package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.wso2.carbon.identity.configuration.mgt.endpoint.*;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.*;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeAddDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ResourceTypeApiService {
    public abstract Response resourceTypePost(ResourceTypeAddDTO type);
    public abstract Response resourceTypePut(ResourceTypeAddDTO type);
    public abstract Response resourceTypeResourceTypeNameDelete(String resourceTypeName);
    public abstract Response resourceTypeResourceTypeNameGet(String resourceTypeName);
}

