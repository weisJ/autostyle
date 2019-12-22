/*
 * Copyright 2019 Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
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
val mdoclet by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    mdoclet("org.jdrupes.mdoclet:doclet:${project.extra["org.jdrupes.mdoclet.version"]}")
}

val mdocletJarFile = "mdocletClasspath.jar"

val generateMDocletPath by tasks.registering(Jar::class) {
    description = "Creates classpath-only jar for running org.jdrupes.mdoclet"
    inputs.files(mdoclet).withNormalizer(ClasspathNormalizer::class.java)
    archiveFileName.set(mdocletJarFile)
    manifest {
        manifest {
            attributes(
                "Main-Class" to "sqlline.SqlLine",
                "Class-Path" to provider { mdoclet.map { it.absolutePath }.joinToString(" ") }
            )
        }
    }
}

tasks.withType<Javadoc>().configureEach {
    dependsOn(generateMDocletPath)
    options.apply {
        doclet = "org.jdrupes.mdoclet.MDoclet"
        docletpath = listOf(File(buildDir, "libs/$mdocletJarFile"))
    }
}
