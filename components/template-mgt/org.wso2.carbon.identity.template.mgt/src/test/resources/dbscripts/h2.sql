/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

CREATE TABLE IF NOT EXISTS IDN_TEMPLATE_MGT (
  TEMPLATE_ID INTEGER NOT NULL AUTO_INCREMENT,
  TENANT_ID INTEGER NOT NULL,
  NAME VARCHAR(255),
  DESCRIPTION VARCHAR(1023),
  TEMPLATE_SCRIPT BLOB NOT NULL,
  PRIMARY KEY (TEMPLATE_ID),
  UNIQUE KEY(TENANT_ID, NAME)
  );
