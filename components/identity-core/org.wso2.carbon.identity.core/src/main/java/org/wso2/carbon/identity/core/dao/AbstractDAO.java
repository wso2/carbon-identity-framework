/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDAO<T> {

    private static final String SQL_GET_ALL_BY_PROP = "SELECT REG_PATH FROM REG_RESOURCE RR, REG_PROPERTY RP WHERE RR.REG_RID=RP.REG_RID AND RP.REG_NAME=? AND RP.REG_PROPERTY_VALUE=?";
    private final static String CUSTOM_QUERY_PATH = IdentityRegistryResources.IDENTITY_PATH
            + "CustomQueries/";
    private final static String CUSTOM_QUERY_GET_ALL_BY_PROP = CUSTOM_QUERY_PATH + "GetByProp";
    private static Log log = LogFactory.getLog(AbstractDAO.class);
    protected Registry registry = null;

    /**
     * @param path
     * @return
     */
    public List<T> getAllObjects(String path) throws IdentityException {
        List<T> list = null;
        Collection collection = null;
        String[] children = null;

        if (log.isErrorEnabled()) {
            log.debug("Retreving all objects from the registry path " + path);
        }

        try {
            list = new ArrayList<T>();

            if (!registry.resourceExists(path)) {
                if (log.isErrorEnabled()) {
                    log.debug("Required resource does bot exist in the registry path " + path);
                }
                return list;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();

            for (String child : children) {
                Resource resource = null;
                resource = registry.get(child);
                T obj = resourceToObject(resource);
                list.add(obj);
            }
        } catch (RegistryException e) {
            log.error("Error while retreving all objects from the registry path", e);
            throw IdentityException.error("Error while retreving all objects from the registry path",
                    e);
        }

        return list;
    }

    /**
     * @param path
     * @param propName
     * @param value
     * @return
     */
    public List<T> getAllObjectsWithPropertyValue(String path, String propName, String value)
            throws IdentityException {
        Resource query = null;
        List<T> retList = null;
        Map<String, String> params = null;
        Resource result = null;
        String[] paths = null;
        Resource resource = null;

        if (log.isErrorEnabled()) {
            log.debug("Retreving all objects from the registry path with property values " + path);
        }

        try {
            retList = new ArrayList<T>();

            if (registry.resourceExists(CUSTOM_QUERY_GET_ALL_BY_PROP)) {
                //query = registry.get(CUSTOM_QUERY_GET_ALL_BY_PROP);
            } else {
                query = registry.newResource();
                query.setContent(SQL_GET_ALL_BY_PROP);
                query.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
                query.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                        RegistryConstants.RESOURCES_RESULT_TYPE);
                registry.put(CUSTOM_QUERY_GET_ALL_BY_PROP, query);
            }

            params = new HashMap<String, String>();
            params.put("1", propName);
            params.put("2", value);
            result = registry.executeQuery(CUSTOM_QUERY_GET_ALL_BY_PROP, params);
            paths = (String[]) result.getContent();

            for (String prop : paths) {
                resource = registry.get(prop);
                retList.add(resourceToObject(resource));
            }
        } catch (RegistryException e) {
            String message = "Error while retreving all objects from the registry path  with property values";
            log.error(message, e);
            throw IdentityException.error(message, e);
        }
        return retList;
    }

    /**
     * @param path
     * @param propName
     * @param value
     * @return
     */
    public T getFirstObjectWithPropertyValue(String path, String propName, String value)
            throws IdentityException {
        Resource resource = null;
        Map<String, String> params = null;
        Resource result = null;
        String[] paths = null;

        try {

            if (log.isErrorEnabled()) {
                log.debug("Retreving first object from the registry path with property value "
                        + path);
            }
            params = new HashMap<String, String>();
            params.put("1", propName);
            params.put("2", value);
            result = registry.executeQuery(getCustomQuery(), params);
            paths = (String[]) result.getContent();

            if (paths != null && paths.length > 0) {
                resource = registry.get(paths[0]);
            }
        } catch (RegistryException e) {
            String message = "Error while retreving first object from the registry path  with property value";
            log.error(message, e);
            throw IdentityException.error(message, e);
        }

        return resourceToObject(resource);
    }

    /**
     * @param resource
     * @return
     */
    protected abstract T resourceToObject(Resource resource);

    private String getCustomQuery() {
        if (StaticConfiguration.isVersioningProperties()) {
            return "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R , REG_PROPERTY PP, REG_RESOURCE_PROPERTY RP"
                    + "R.REG_VERSION=RP.REG_VERSION AND"
                    + "RP.REG_PROPERTY_ID=PP.REG_ID AND PP.REG_NAME=? AND PP.REG_VALUE LIKE ?";
        } else {
            return "SELECT R.REG_PATH_ID, R.REG_NAME FROM REG_RESOURCE R , REG_PROPERTY PP, REG_RESOURCE_PROPERTY RP"
                    + "((R.REG_PATH_ID=RP.REG_PATH_ID AND R.REG_NAME = RP.REG_RESOURCE_NAME ) OR"
                    + "(R.REG_PATH_ID=RP.REG_PATH_ID AND R.REG_NAME IS NULL AND RP.REG_RESOURCE_NAME IS NULL)) AND"
                    + "RP.REG_PROPERTY_ID=PP.REG_ID AND PP.REG_NAME=? AND PP.REG_VALUE LIKE ?";
        }
    }

}
