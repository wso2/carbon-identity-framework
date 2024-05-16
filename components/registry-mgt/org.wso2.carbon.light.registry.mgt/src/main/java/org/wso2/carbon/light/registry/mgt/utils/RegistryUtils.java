package org.wso2.carbon.light.registry.mgt.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.light.registry.mgt.LightRegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.PATH_SEPARATOR;
import static org.wso2.carbon.light.registry.mgt.constants.LightRegistryConstants.ROOT_PATH;

public final class RegistryUtils {

    private static final Log log = LogFactory.getLog(RegistryUtils.class);

    private RegistryUtils() {

    }

    /**
     * Method to obtain the absolute path of the given resource path.
     *
     * @param path the resource path.
     * @return the parent path.
     */
    public static String getAbsolutePath(String path) {

        // TODO: check if we can get this dynamically
        return "/_system/config" + path;
    }

    /**
     * Method to obtain the parent path of the given resource path.
     *
     * @param resourcePath the resource path.
     * @return the parent path.
     */
    public static String getParentPath(String resourcePath) {

        if (resourcePath == null) {
            return null;
        }

        String parentPath;
        if (resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            parentPath = null;
        } else {
            String formattedPath = resourcePath;
            if (resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                formattedPath = resourcePath.substring(
                        0, resourcePath.length() - RegistryConstants.PATH_SEPARATOR.length());
            }

            if (formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) <= 0) {
                parentPath = RegistryConstants.ROOT_PATH;
            } else {
                parentPath = formattedPath.substring(
                        0, formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            }
        }

        return parentPath;
    }

    /**
     * Returns resource name when full resource path is passed.
     *
     * @param resourcePath full resource path.
     * @return the resource name.
     */
    public static String getResourceName(String resourcePath) {

        String resourceName;
        if (resourcePath.equals(RegistryConstants.ROOT_PATH)) {
            resourceName = RegistryConstants.ROOT_PATH;

        } else {

            String formattedPath = resourcePath;
            if (resourcePath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                formattedPath = resourcePath.substring(
                        0, resourcePath.length() - RegistryConstants.PATH_SEPARATOR.length());
            }

            if (formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) == 0) {
                resourceName = formattedPath.substring(1);
            } else {
                resourceName = formattedPath.substring(
                        formattedPath.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            }
        }

        return resourceName;
    }

    public static byte[] encodeString(String content) {

        return content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert an input stream into a byte array.
     *
     * @param inputStream the input stream.
     * @return the byte array.
     * @throws LightRegistryException if the operation failed.
     */
    public static byte[] getByteArray(InputStream inputStream) throws LightRegistryException {

        if (inputStream == null) {
            String msg = "Could not create memory based content for null input stream.";
            log.error(msg);
            throw new LightRegistryException(msg);
        }

        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            byte[] contentChunk = new byte[RegistryConstants.DEFAULT_BUFFER_SIZE];
            int byteCount;
            while ((byteCount = inputStream.read(contentChunk)) != -1) {
                out.write(contentChunk, 0, byteCount);
            }
            out.flush();

            return out.toByteArray();

        } catch (IOException e) {
            String msg = "Failed to write data to byte array input stream. " + e.getMessage();
            log.error(msg, e);
            throw new LightRegistryException(msg, e);
        } finally {
            try {
                inputStream.close();
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                String msg = "Failed to close streams used for creating memory stream. "
                        + e.getMessage();
                log.error(msg, e);
            }
        }
    }

    /**
     * All "valid" paths pure resources should be in the form /c1/c2/r1. That is they should start
     * with "/" and should not end with "/". Given a path of a pure resource, this method prepares
     * the valid path for that path.
     *
     * @param resourcePath Path of a pure resource.
     * @return Valid path of the pure resource.
     */
    public static String getPureResourcePath(String resourcePath) {

        if (resourcePath == null) {
            return null;
        }

        String preparedPath = resourcePath;
        if (preparedPath.equals(ROOT_PATH)) {
            return preparedPath;
        } else {
            if (!preparedPath.startsWith(ROOT_PATH)) {
                preparedPath = ROOT_PATH + preparedPath;
            }
            if (preparedPath.endsWith(PATH_SEPARATOR)) {
                preparedPath = preparedPath.substring(0, preparedPath.length() - 1);
            }
            if (preparedPath.contains("//")) {
                preparedPath = preparedPath.replace("//", "/");
            }
        }

        return preparedPath;
    }
}
