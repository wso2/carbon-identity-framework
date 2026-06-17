package org.wso2.carbon.identity.core.context.valve;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Vector;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdentityContextCreatorValveTest {

    private IdentityContextCreatorValve identityContextCreatorValve;

    @Mock
    private Valve nextValve;

    private AutoCloseable closeable;

    @BeforeMethod
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        identityContextCreatorValve = new IdentityContextCreatorValve();
        identityContextCreatorValve.setNext(nextValve);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testInvokeWithMultipleAuthorizationHeaders() throws Exception {
        Vector<String> headers = new Vector<>();
        headers.add("Bearer token1");
        headers.add("Bearer token2");
        final Enumeration<String> headerEnum = headers.elements();

        Request request = new Request(null) {
            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("Authorization".equals(name)) {
                    return headerEnum;
                }
                return super.getHeaders(name);
            }

            @Override
            public String getRequestURI() {
                return "/oauth2/userinfo";
            }
        };

        final int[] errorStatus = new int[1];
        final String[] errorMessage = new String[1];
        Response response = new Response() {
            @Override
            public void sendError(int status, String message) throws java.io.IOException {
                errorStatus[0] = status;
                errorMessage[0] = message;
            }
        };

        identityContextCreatorValve.invoke(request, response);

        org.testng.Assert.assertEquals(errorStatus[0], HttpServletResponse.SC_BAD_REQUEST);
        org.testng.Assert.assertEquals(errorMessage[0], "Multiple Authorization headers are not allowed.");
        verify(nextValve, never()).invoke(any(Request.class), any(Response.class));
    }
}
