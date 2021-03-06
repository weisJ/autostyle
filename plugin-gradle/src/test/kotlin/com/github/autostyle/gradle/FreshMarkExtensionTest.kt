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
package com.github.autostyle.gradle

import org.junit.jupiter.api.Test

class FreshMarkExtensionTest : GradleIntegrationTest() {
    @Test
    fun integration() {
        setFile("build.gradle").toContent(
            """
            plugins {
                id 'java'
                id 'com.github.autostyle'
            }
            repositories { mavenCentral() }
            autostyle {
                freshmark {
                    properties {
                        it.put('lib', 'MyLib')
                        it.put('author', 'Me')
                    }
                }
            }
            """.trimIndent()
        )
        setFile("README.md").toResource("freshmark/FreshMarkUnformatted.test")
        gradleRunner().withArguments("autostyleApply").build()
        assertFile("README.md").sameAsResource("freshmark/FreshMarkFormatted.test")
    }
}
