/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.autostyle.gradle;

import java.io.IOException;

import org.junit.Test;

public class SqlExtensionTest extends GradleIntegrationTest {

  @Test
  public void should_format_sql_with_default_configuration() throws IOException {
    setFile("build.gradle").toLines(
        "plugins {",
        "    id 'com.github.autostyle'",
        "}",
        "autostyle {",
        "    sql {",
        "       dbeaver()",
        "    }",
        "}");

    setFile("src/main/resources/aFolder/create.sql").toResource("sql/dbeaver/create.dirty");
    gradleRunner().withArguments("autostyleApply").build();
    assertFile("src/main/resources/aFolder/create.sql").sameAsResource("sql/dbeaver/create.clean");
  }

  @Test
  public void should_format_sql_with_alternative_configuration() throws IOException {
    setFile("build.gradle").toLines(
        "plugins {",
        "    id 'com.github.autostyle'",
        "}",
        "autostyle {",
        "    sql {",
        "       dbeaver {",
        "           configFiles('myConfig.properties')",
        "       }",
        "    }",
        "}");
    setFile("myConfig.properties").toResource("sql/dbeaver/sqlConfig2.properties");

    setFile("src/main/resources/aFolder/create.sql").toResource("sql/dbeaver/create.dirty");
    gradleRunner().withArguments("autostyleApply").build();
    assertFile("src/main/resources/aFolder/create.sql").sameAsResource("sql/dbeaver/create.clean.alternative");
  }
}
