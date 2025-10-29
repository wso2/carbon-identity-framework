/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.role.v2.mgt.core.dao;

/**
 * Role management DAO factory.
 */
public class RoleMgtDAOFactory {

    private static RoleMgtDAOFactory factory = new RoleMgtDAOFactory();
    private RoleDAO roleDAO;
    private RoleDAO cacheBackedRoleDAO;
    private GroupDAO groupDAO;

    private RoleMgtDAOFactory() {

        this.roleDAO = new RoleDAOImpl();
        this.cacheBackedRoleDAO = new CacheBackedRoleDAO();
        this.groupDAO = new GroupDAOImpl();
    }

    public static RoleMgtDAOFactory getInstance() {

        return factory;
    }

    public RoleDAO getRoleDAO() {

        return roleDAO;
    }

    public RoleDAO getCacheBackedRoleDAO() {

        return cacheBackedRoleDAO;
    }

    public GroupDAO getGroupDAO() {

        return groupDAO;
    }
}
