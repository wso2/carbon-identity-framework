/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.event.publisher.api.model.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Model Class Implementation for SimpleSubject.
 */
public class SimpleSubject extends Subject {

    private static final String EMAIL = "email";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String ACCOUNT = "account";
    private static final String URI = "uri";
    private static final String ISS_SUB = "iss_sub";
    private static final String OPAQUE = "opaque";
    private static final String ID = "id";
    private static final String DID = "did";
    private static final String ISS = "iss";
    private static final String SUB = "sub";
    private static final String ALIASES = "aliases";
    private static final String IDENTIFIERS = "identifiers";

    private static boolean isInvalidValue(String value) {

        return StringUtils.isBlank(value);
    }

    private SimpleSubject() {

    }

    public static SimpleSubject createEmailSubject(String email) {

        if (isInvalidValue(email)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(EMAIL);
        subject.addProperty(EMAIL, email);
        return subject;
    }

    public static SimpleSubject createPhoneSubject(String phoneNumber) {

        if (isInvalidValue(phoneNumber)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(PHONE_NUMBER);
        subject.addProperty(PHONE_NUMBER, phoneNumber);
        return subject;
    }

    public static SimpleSubject createAccountSubject(String uri) {

        if (isInvalidValue(uri)) {
            return null;
        }

        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(ACCOUNT);
        subject.addProperty(URI, uri);
        return subject;
    }

    public static SimpleSubject createIssSubSubject(String iss, String sub) {

        if (isInvalidValue(iss) || isInvalidValue(sub)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(ISS_SUB);
        subject.addProperty(ISS, iss);
        subject.addProperty(SUB, sub);
        return subject;
    }

    public static SimpleSubject createOpaqueSubject(String id) {

        if (isInvalidValue(id)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(OPAQUE);
        subject.addProperty(ID, id);
        return subject;
    }

    public static SimpleSubject createDIDSubject(String url) {

        if (isInvalidValue(url)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(DID);
        subject.addProperty(DID, url);
        return subject;
    }

    public static SimpleSubject createURISubject(String uri) {

        if (isInvalidValue(uri)) {
            return null;
        }
        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(URI);
        subject.addProperty(URI, uri);
        return subject;
    }

    public static SimpleSubject createAliasesSubject(List<SimpleSubject> identifiers) {

        if (CollectionUtils.isEmpty(identifiers)) {
            return null;
        }
        for (SimpleSubject identifier : identifiers) {
            if (identifier.getFormat().equals(ALIASES)) {
                throw new IllegalArgumentException("Identifier format cannot be " + ALIASES);
            }
        }

        // Remove duplicates
        Set<SimpleSubject> uniqueIdentifiers = new HashSet<>(identifiers);
        identifiers.clear();
        identifiers.addAll(uniqueIdentifiers);

        SimpleSubject subject = new SimpleSubject();
        subject.setFormat(ALIASES);
        subject.addProperty(IDENTIFIERS, identifiers);
        return subject;
    }
}
