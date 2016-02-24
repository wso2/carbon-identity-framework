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

/**
 * This class defines static constants used in LDAP server manager component.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class LDAPServerManagerConstants {

    public static final String SERVICE_PASSWORD_REGEX_PROPERTY = "ServicePasswordJavaRegEx";
    public static final String SERVICE_PRINCIPLE_NAME_REGEX_PROPERTY = "ServiceNameJavaRegEx";
    public static final String DEFAULT_PASSWORD_REGULAR_EXPRESSION = "[\\\\S]{5,30}";
    public static final String DEFAULT_SERVICE_NAME_REGULAR_EXPRESSION = "[a-zA-Z\\d]{2,10}/[a-zA-Z]{2,30}";
    public static final String SERVER_PRINCIPAL_ATTRIBUTE_VALUE = "Service";
    public static final String SERVER_PRINCIPAL_ATTRIBUTE_NAME = "sn";
    public static final String PASSWORD_HASH_METHOD = "passwordHashMethod";
    public static final String KRB5_PRINCIPAL_NAME_ATTRIBUTE = "krb5PrincipalName";
    public static final String KRB5_KEY_VERSION_NUMBER_ATTRIBUTE = "krb5KeyVersionNumber";
    public static final String PASSWORD_HASH_METHOD_PLAIN_TEXT = "PlainText";
    public static final String KERBEROS_TGT = "krbtgt";
    //LDAP constants
    public static final String LDAP_UID = "uid";
    public static final String LDAP_PASSWORD = "userPassword";
    public static final String LDAP_OBJECT_CLASS = "objectClass";
    public static final String LDAP_INTET_ORG_PERSON = "inetOrgPerson";
    public static final String LDAP_ORG_PERSON = "organizationalPerson";
    public static final String LDAP_PERSON = "person";
    public static final String LDAP_TOP = "top";
    public static final String LDAP_KRB5_PRINCIPLE = "krb5principal";
    public static final String LDAP_KRB5_KDC = "krb5kdcentry";
    public static final String LDAP_SUB_SCHEMA = "subschema";
    public static final String LDAP_COMMON_NAME = "cn";
    // For back-end we have to use following default values
    public static final String DEFAULT_BE_PASSWORD_REGULAR_EXPRESSION = "[\\S]{5,30}";
    public static final String DEFAULT_BE_SERVICE_NAME_REGULAR_EXPRESSION = DEFAULT_SERVICE_NAME_REGULAR_EXPRESSION;

    private LDAPServerManagerConstants() {
    }
}
