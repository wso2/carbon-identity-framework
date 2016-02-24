/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.directory.server.manager.internal;

import junit.framework.TestCase;
import org.wso2.carbon.directory.server.manager.DirectoryServerManagerException;
import org.wso2.carbon.directory.server.manager.common.ServerPrinciple;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.ldap.LDAPConstants;

import java.util.HashMap;
import java.util.Map;

public class LDAPServerStoreManagerTest extends TestCase {

    private RealmConfiguration realmConfiguration;

    public void setUp() {

        this.realmConfiguration = new RealmConfiguration();

        Map<String,String> map = new HashMap<String, String>();
        map.put(UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX, "[\\S]{5,30}");
        map.put(LDAPConstants.USER_SEARCH_BASE, "ou=Users,dc=example,dc=com");
        map.put("PASSWORD_HASH_METHOD", "PlainText");
        map.put("DEFAULT_REALM_NAME", "EXAMPLE..COM");
        map.put(LDAPConstants.CONNECTION_URL, "ldap://localhost:10389");
        map.put(LDAPConstants.CONNECTION_NAME, "uid=admin,ou=system");
        map.put(LDAPConstants.CONNECTION_PASSWORD, "secret");
        map.put(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST, "50");
        map.put(LDAPConstants.USER_NAME_LIST_FILTER, "(objectClass=person)");


        this.realmConfiguration.setUserStoreProperties(map);
        
    }

    public void testAddServicePrinciple() throws DirectoryServerManagerException {

        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        manager.addServicePrinciple("ftpdd/localhost", "Test FTP Server", "qawsedrf");
        manager.deleteServicePrinciple("ftpdd/localhost");
    }

    public void testGetServiceUid() throws DirectoryServerManagerException {
        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        String serviceUid = manager.getServiceName("ssh/localhost");
        assertEquals(serviceUid, "ssh");        
    }

    public void testExistingServiceUid() throws DirectoryServerManagerException {
        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        manager.addServicePrinciple("ppp/localhost", "Test FTP Server", "qawsedrf");
        assertTrue(manager.isExistingServiceUid("ppp"));
        manager.deleteServicePrinciple("ppp/localhost");
    }

    public void testExistingService() throws DirectoryServerManagerException {
        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);

        manager.addServicePrinciple("mms/localhost", "Test FTP Server", "qawsedrf");
        assertTrue(manager.isExistingServicePrinciple("mms/localhost"));
        manager.deleteServicePrinciple("mms/localhost");
    }

    public void testUpdatePassword() throws DirectoryServerManagerException {
        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        manager.addServicePrinciple("lgp/localhost", "Test FTP Server", "qawsedrf");
        manager.updateServicePrinciplePassword("lgp/localhost", "qawsedrf", "a1b2c3d4e5");
        manager.deleteServicePrinciple("lgp/localhost");

    }

    public void testPasswordValidity() throws DirectoryServerManagerException {

        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);

        manager.addServicePrinciple("tts/localhost", "Test FTP Server", "qawsedrf");
        assertTrue(manager.isValidPassword("tts/localhost", "qawsedrf"));
        //assertTrue(manager.isValidPassword("tts/localhost", "qawsedrf1"));

    }

    public void testDeleteServicePrinciple() throws DirectoryServerManagerException {

        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        manager.deleteServicePrinciple("tts/localhost");
    }

    public void testListLDAPPrinciples() throws DirectoryServerManagerException {
        LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
        ServerPrinciple[] principles = manager.listServicePrinciples("*");
        for (ServerPrinciple principle : principles) {
            System.out.println(principle.getServerName() + ", " + principle.getServerDescription());
        }
    }

   /* public void testRealmName () {
       LDAPServerStoreManager manager = new LDAPServerStoreManager(this.realmConfiguration);
       String realmName = manager.getRealmName();
        System.out.println(realmName);
    }*/
}
