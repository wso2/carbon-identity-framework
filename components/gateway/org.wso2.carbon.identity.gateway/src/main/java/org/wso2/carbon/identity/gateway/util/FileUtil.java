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

package org.wso2.carbon.identity.gateway.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Config file read utility.
 */
public class FileUtil {

    private FileUtil() {

    }

    /**
     * Read a.yaml file according to a class type.
     *
     * @param filePath  File path of the configuration file
     * @param classType Class type of the.yaml bean
     * @param <T>       Class T
     * @return Config file bean
     */
    public static <T> T readConfigFile(String filePath, Class<T> classType)
             {

        Path file = Paths.get(filePath);

        return readConfigFile(file, classType);
    }

    /**
     * Read a.yaml file according to a class type.
     *
     * @param file      File path of the configuration file
     * @param classType Class type of the.yaml bean
     * @param <T>       Class T
     * @return Config file bean
     */
    public static <T> T readConfigFile(Path file, Class<T> classType)
             {

        if (Files.exists(file)) {
            try {
                Reader in = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                return yaml.loadAs(in, classType);
            } catch (IOException e) {
            }
        } else {

        }
        return null;
    }

    /**
     * Read a.yaml file according to a class type.
     *
     * @param path          folder which contain the config files
     * @param classType     Class type of the.yaml bean
     * @param fileNameRegex file name regex
     * @param <T>           Class T
     * @return Config file bean
     */
    public static <T> List<T> readConfigFiles(Path path, Class<T> classType, String fileNameRegex)
          {

        List<T> configEntries = new ArrayList<>();
        if (Files.exists(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, fileNameRegex)) {
                for (Path file : stream) {
                    Reader in = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);
                    Yaml yaml = new Yaml();
                    yaml.setBeanAccess(BeanAccess.FIELD);
                    configEntries.add(yaml.loadAs(in, classType));
                }
            } catch (DirectoryIteratorException | IOException e) {
            }
        }
        return configEntries;
    }
}
