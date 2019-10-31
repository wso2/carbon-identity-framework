package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.wso2.carbon.identity.configuration.mgt.endpoint.*;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.*;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceFileDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ResourceApiService {
    public abstract Response resourceResourceTypePost(String resourceType,ResourceAddDTO resource);
    public abstract Response resourceResourceTypePut(String resourceType,ResourceAddDTO resource);
    public abstract Response resourceResourceTypeResourceNameAttributeKeyDelete(String resourceName,String resourceType,String attributeKey);
    public abstract Response resourceResourceTypeResourceNameAttributeKeyGet(String resourceName,String resourceType,String attributeKey);
    public abstract Response resourceResourceTypeResourceNameDelete(String resourceName,String resourceType);
    public abstract Response resourceResourceTypeResourceNameFileDelete(String resourceName,String resourceType);
    public abstract Response resourceResourceTypeResourceNameFileFileIdDelete(String fileId,String resourceType,String resourceName);
    public abstract Response resourceResourceTypeResourceNameFileFileIdGet(String fileId,String resourceType,String resourceName);
    public abstract Response resourceResourceTypeResourceNameFileGet(String resourceName,String resourceType);
    public abstract Response resourceResourceTypeResourceNameFilePost(String resourceName,String resourceType,InputStream resourceFileInputStream,Attachment resourceFileDetail,String fileName);
    public abstract Response resourceResourceTypeResourceNameGet(String resourceName,String resourceType);
    public abstract Response resourceResourceTypeResourceNamePost(String resourceName,String resourceType,AttributeDTO attribute);
    public abstract Response resourceResourceTypeResourceNamePut(String resourceName,String resourceType,AttributeDTO attribute);
}

