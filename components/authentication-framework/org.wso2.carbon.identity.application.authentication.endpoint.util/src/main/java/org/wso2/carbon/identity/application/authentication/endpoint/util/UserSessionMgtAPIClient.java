package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Client for calling User Session Management API with mutual SSL authentication
 */
public class UserSessionMgtAPIClient {

    private static final Log log = LogFactory.getLog(UserSessionMgtAPIClient.class);

    private static final String HTTP_METHOD_DELETE = "DELETE";

    /**
     * Send mutual SSL https delete request
     *
     * @param backendURL URL of the service
     * @throws IOException
     */
    public static void terminateUserSession(String backendURL) {

        URL url = null;

        try {
            url = new URL(backendURL);
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            httpsUrlConnection.setDoOutput(true);
            httpsUrlConnection.setSSLSocketFactory(MutualSSLManager.getSslSocketFactory());
            httpsUrlConnection.setRequestMethod(HTTP_METHOD_DELETE);
            httpsUrlConnection.setRequestProperty(MutualSSLManager.getUsernameHeaderName(),
                    MutualSSLManager.getCarbonLogin());
            httpsUrlConnection.getResponseCode();
        } catch (IOException e) {
            log.error("Sending " + HTTP_METHOD_DELETE + " request to URL : " + url + "failed.", e);
        }
    }

}
