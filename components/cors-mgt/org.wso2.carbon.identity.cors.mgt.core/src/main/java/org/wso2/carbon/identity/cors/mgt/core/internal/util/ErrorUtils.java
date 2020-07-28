package org.wso2.carbon.identity.cors.mgt.core.internal.util;

import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;

/**
 * Error utilities.
 */
public class ErrorUtils {

    /**
     * Handle server exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceServerException instance.
     */
    public static CORSManagementServiceServerException handleServerException(ErrorMessages error, String... data) {

        return new CORSManagementServiceServerException(error.getCode(), String.format(error.getDescription(), data));
    }

    /**
     * Handle server exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     Original error.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceServerException instance.
     */
    public static CORSManagementServiceServerException handleServerException(ErrorMessages error, Throwable e,
                                                                             String... data) {

        return new CORSManagementServiceServerException(error.getCode(), String.format(error.getDescription(), data),
                e);
    }

    /**
     * Handle client exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceClientException instance.
     */
    public static CORSManagementServiceClientException handleClientException(ErrorMessages error, String... data) {

        return new CORSManagementServiceClientException(error.getCode(), String.format(error.getDescription(), data));
    }

    /**
     * Handle client exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     Original error.
     * @param data  Additional data that should be added to the error message. This is a String var-arg.
     * @return CORSManagementServiceClientException instance.
     */
    public static CORSManagementServiceClientException handleClientException(ErrorMessages error, Throwable e,
                                                                             String... data) {

        return new CORSManagementServiceClientException(error.getCode(), String.format(error.getDescription(), data),
                e);
    }
}
