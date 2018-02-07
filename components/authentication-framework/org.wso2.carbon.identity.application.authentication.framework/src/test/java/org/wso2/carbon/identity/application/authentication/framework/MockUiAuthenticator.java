package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.SubjectCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockUiAuthenticator extends AbstractApplicationAuthenticator {

    private String name;
    private SubjectCallback subjectCallback;

    public MockUiAuthenticator(String name, SubjectCallback subjectCallback) {
        this.name = name;
        this.subjectCallback = subjectCallback;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {
        super.initiateAuthenticationRequest(request, response, context);
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {
        if (subjectCallback != null) {
            context.setSubject(subjectCallback.getAuthenticatedUser(context));
        }
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        return request.getParameter("returning") != null;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFriendlyName() {
        return name;
    }
}
