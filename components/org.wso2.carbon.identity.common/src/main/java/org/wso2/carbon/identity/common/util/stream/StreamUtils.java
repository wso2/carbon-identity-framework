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

package org.wso2.carbon.identity.common.util.stream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Stream utils.
 */
public class StreamUtils {

    private static final Logger logger = LoggerFactory.getLogger(StreamUtils.class);
    private static volatile StreamUtils instance = new StreamUtils();

    private StreamUtils() {

    }

    public static StreamUtils getInstance() {
        if (instance == null) {
            synchronized (StreamUtils.class) {
                if (instance == null) {
                    instance = new StreamUtils();
                }
            }
        }
        return instance;
    }

    public void closeAllStreams(InputStream input, OutputStream output) {
        closeInputStream(input);
        closeOutputStream(output);
    }

    public void closeInputStream(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            logger.error("Error occurred while closing Input stream");
        }
    }

    public void closeOutputStream(OutputStream output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            logger.error("Error occurred while closing Output stream");
        }
    }

    public void flushOutputStream(OutputStream output) {
        try {
            if (output != null) {
                output.flush();
            }
        } catch (IOException ioe) {
            logger.error("Error occurred while flushing Output stream");
        }
    }

    public void closeReader(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ioe) {
            logger.error("Error occurred while closing Reader");
        }
    }
}
