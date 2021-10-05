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
package org.wso2.carbon.identity.user.store.configuration.ui;

public final class UserStoreUIConstants {
    private UserStoreUIConstants() {
    }

    public static final String RWLDAP_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager";
    public static final String URWLDAP_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap" +
            ".UniqueIDReadWriteLDAPUserStoreManager";
    public static final String ROLDAP_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ReadOnlyLDAPUserStoreManager";
    public static final String ACTIVEDIRECTORY_USERSTORE_MANAGER = "org.wso2.carbon.user.core.ldap.ActiveDirectoryUserStoreManager";
    public static final String JDABC_USERSTORE_MANAGER = "org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager";
    public static final String CASSANDRA_USERSTORE_MANAGER = "org.wso2.carbon.user.cassandra.CassandraUserStoreManager";
    public static final String PRIMARY = "PRIMARY";
    public static final String CLASS = "Class";
    public static final String DESCRIPTION = "Description";
    public static final String DISABLED = "Disabled";
    public static final String DOMAIN = "DomainName";




}