package org.wso2.carbon.idp.mgt.ui.util;

import org.apache.commons.collections.functors.ExceptionClosure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by pasindu on 10/24/16.
 */
public class MetadataDownloadHandler {


    private static Log log = LogFactory.getLog(MetadataDownloadHandler.class);

    public String createFile(String metadata, String fileName) {
        File file = new File(fileName);
        try {

            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println(metadata);
            writer.close();
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Error creating metadata.xml file");
            }
        }

        return file.getAbsolutePath();
    }

    public void downloadFile(String fileName, HttpServletResponse response) {


    }

}
