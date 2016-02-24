
/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.core;

import junit.framework.TestCase;
import org.wso2.carbon.user.core.ldap.LDAPConstants;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

public class LDAPTestCase extends TestCase {
	public void testLDAP() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://localhost:10389");

		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system"); // specify
		                                                            // the
		                                                            // user name
		env.put(Context.SECURITY_CREDENTIALS, "admin"); // specify the
		                                                // password

		boolean save = false;

		try {
			DirContext dirContext = new InitialDirContext(env);

			if (save) {

				// create the attribute set for group entry
				Attributes groupAttributes = new BasicAttributes(true);

				// create group entry's object class attribute
				Attribute objectClassAttribute =
				                                 new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
				objectClassAttribute.add("groupOfNames");
				groupAttributes.put(objectClassAttribute);

				// create cn attribute
				String groupNameAttributeName = "cn";
				Attribute cnAttribute = new BasicAttribute(groupNameAttributeName);
				cnAttribute.add("buduammo");
				groupAttributes.put(cnAttribute);

				DirContext grpCtx = (DirContext) dirContext.lookup("ou=SharedGroups,dc=wso2,dc=org");
				NameParser ldapParser = grpCtx.getNameParser("");
				/*
				 * Name compoundGroupName =
				 * ldapParser.parse(groupNameAttributeName
				 * + "=" +
				 * roleName);
				 */
				
				Name compoundGroupName = ldapParser.parse("cn=buduammo");
				grpCtx.bind(compoundGroupName, null, groupAttributes);

				System.out.println("=====================" + dirContext);
			} else {

				String searchFilter = "(objectClass=groupOfNames)";
				String roleNameProperty = "cn";
				String dn = "distinguishedName";
				String filter = "*";
				SearchControls searchCtls = new SearchControls();
				searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchCtls.setCountLimit(1000);
				searchCtls.setTimeLimit(1000);

				String returnedAtts[] = { roleNameProperty, dn };
				searchCtls.setReturningAttributes(returnedAtts);

				// / search filter TODO
				StringBuffer buff = new StringBuffer();
				buff.append("(&").append(searchFilter).append("(").append(roleNameProperty)
				    .append("=").append(filter).append("))");

				NamingEnumeration<SearchResult> answer = null;

				try {
					answer =
					         dirContext.search("ou=SharedGroups,dc=wso2,dc=org", buff.toString(),
					                           searchCtls);
					while (answer.hasMoreElements()) {
						SearchResult sr = (SearchResult) answer.next();
						System.out.println(sr.getNameInNamespace());
						if (sr.getAttributes() != null) {

							NamingEnumeration<?> en = sr.getAttributes().getIDs();
							while (en.hasMore()) {
								String name = en.next().toString();
								Attribute attr = sr.getAttributes().get(name);
								if (attr != null) {
									String val = (String) attr.get();
									System.out.println(name + ": " + val);

								} else {
									System.out.println(name + ": null");
								}
							}
							//
							// Attribute attr =
							// sr.getAttributes().get(roleNameProperty);
							// if (attr != null) {
							// String name = (String) attr.get();
							// System.out.println(name);
							// // append the domain if exist
							// }
							//
							// attr = sr.getAttributes().get(dn);
							// System.out.println(attr);
							// if (attr != null) {
							// String name = (String) attr.get();
							// System.out.println(name);
							// // append the domain if exist
							// }
						}
					}
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class ABC {

		public ABC(String name) {
			super();
			this.name = name;
		}

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
