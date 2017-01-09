/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.element.response;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.framework.message.Response;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.util.SAMLUtils;

import javax.naming.ConfigurationException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.Status.OK;
import static org.wso2.carbon.identity.gateway.util.SAMLConstants.SAML_AUTH_REQUEST;

public class SAMLResponseHandler extends AbstractGatewayHandler {

    private static final Logger logger = LoggerFactory.getLogger(SAMLResponseHandler.class);

    // TODO : Read these from the config model
    private final String ACS_URL = "http://localhost:8080/travelocity.com/home.jsp";
    private final String SAML_ISSUER = "localhost";
    private final int RESPONSE_VALIDITY_PERIOD_MINUTES = 30;
    private final String NAME_ID_FORMAT = NameID.UNSPECIFIED;
    private final String SUBJECT_CONFIRMATION = SubjectConfirmation.METHOD_BEARER;
    private final String AUDIENCE = "travelocity.com";



    @Override
    public HandlerResponseStatus handle(GatewayMessageContext context) {

        String sessionID = context.getSessionDataKey();

        try {
            if (StringUtils.isNotBlank(sessionID)) {
                AuthnRequest authnRequest = null;

                Map<String, Object> contextMap = (Map<String, Object>) context.getParameter(sessionID);
                if (contextMap == null) {
                    throw new IllegalArgumentException("Context map null. Cannot find context information");
                }

                Object request = contextMap.getOrDefault(SAML_AUTH_REQUEST, null);
                if (request != null && request instanceof AuthnRequest) {
                    authnRequest = (AuthnRequest) request;
                } else {
                    throw new FrameworkRuntimeException("Cannot find the SAML Authentication Request in the context.");
                }

                String subject = (String) contextMap.getOrDefault("subject", null);
                Map<String, String> claimMap = (Map<String, String>) contextMap.getOrDefault("claims", new HashMap<>());

                String samlReponse = buildSAMLResponse(authnRequest, ACS_URL, subject, claimMap);
                if (StringUtils.isBlank(samlReponse)) {
                    throw new IllegalArgumentException("Cannot build a SAML Response.");
                }

                String samlHtmlResponseBody = SAMLUtils.getHTMLResponseBody(samlReponse);

                Response response = context.getCurrentIdentityResponse();
                response.setStatusCode(OK.getStatusCode());
                response.setBody(samlHtmlResponseBody);

            } else {
                throw new IllegalArgumentException("Session Context Information not available.");
            }

        } catch (IllegalArgumentException | ConfigurationException ex) {
            logger.error("Error while building SAML Response.", ex);
            return HandlerResponseStatus.SUSPEND;
        }

        return HandlerResponseStatus.CONTINUE;
    }

    @Override
    public boolean canHandle(GatewayMessageContext messageContext) {

        return true;
    }


    private String buildSAMLResponse(AuthnRequest authnRequest,
                                     String assertionConsumerUrl,
                                     String subjectIdentifier,
                                     Map<String, String> claimMap) throws ConfigurationException {

        org.opensaml.saml2.core.Response response = new ResponseBuilder().buildObject();

        response.setID(UUID.randomUUID().toString());
        response.setVersion(SAMLVersion.VERSION_20);

        // set issuer element
        Issuer issuer = SAMLUtils.getIssuer(SAML_ISSUER, NameIDType.ENTITY);
        response.setIssuer(issuer);

        String inResponseTo = authnRequest.getID();
        response.setInResponseTo(inResponseTo);

        response.setDestination(authnRequest.getAssertionConsumerServiceURL());

        // set the Response status
        Status status = new StatusBuilder().buildObject();
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statCode);

        // set issue instant
        DateTime issueInstant = new DateTime();
        response.setIssueInstant(issueInstant);
        DateTime notOnOrAfter = new DateTime(issueInstant.getMillis() + RESPONSE_VALIDITY_PERIOD_MINUTES * 60 * 1000);

        /*
            Assertion Building
         */
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setID(UUID.randomUUID().toString());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(SAMLUtils.getIssuer(SAML_ISSUER, NameIDType.ENTITY));
        assertion.setIssueInstant(issueInstant);

        /*
            Subject
         */
        Subject subject = new SubjectBuilder().buildObject();
        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setValue(subjectIdentifier);
        nameId.setFormat(NAME_ID_FORMAT);
        subject.setNameID(nameId);

        /*
            Subject Confirmation Data
         */
        SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
        subjectConfirmation.setMethod(SUBJECT_CONFIRMATION);
        SubjectConfirmationData subjectConfirmationData = new SubjectConfirmationDataBuilder().buildObject();
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setNotBefore(notOnOrAfter);
        subjectConfirmationData.setRecipient(assertionConsumerUrl);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);


        /*
            Authentication Statement
         */
        AuthnStatement authnStatement = new AuthnStatementBuilder().buildObject();
        authnStatement.setAuthnInstant(issueInstant);

        AuthnContext authnContext = new AuthnContextBuilder().buildObject();
        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);

        assertion.getAuthnStatements().add(authnStatement);

        /*
            Attribute Statements
         */
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        Optional.ofNullable(claimMap).ifPresent(map -> map.forEach((x, y) -> {
            Attribute attribute = new AttributeBuilder().buildObject();
            attribute.setNameFormat(Attribute.BASIC);
            attribute.setName(x);

            XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder
                    (XSString.TYPE_NAME);
            XSString stringValue =
                    stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            stringValue.setValue(y);
            attribute.getAttributeValues().add(stringValue);
            attributeStatement.getAttributes().add(attribute);
        }));

        assertion.getAttributeStatements().add(attributeStatement);

        /*
            Conditions
         */
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(issueInstant);
        conditions.setNotOnOrAfter(notOnOrAfter);

        /*
            Audience restriction
         */
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(AUDIENCE);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);

        assertion.setConditions(conditions);
        response.getAssertions().add(assertion);

        // TODO : Encryption, Signing need to be added.
        String samlResponse = SAMLUtils.marshall(response);
        if (StringUtils.isNotBlank(samlResponse)) {
            return Base64.getEncoder().encodeToString(samlResponse.getBytes(UTF_8));
        } else {
            return null;
        }
    }
}
