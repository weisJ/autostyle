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
package com.github.autostyle

import com.diffplug.common.base.StandardSystemProperty
import com.diffplug.common.io.Files
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path
import java.util.*
import java.util.function.Supplier

object TestProvisioner {
    @JvmStatic
    fun gradleProject(dir: File): Project {
        val userHome = File(StandardSystemProperty.USER_HOME.value())
        return ProjectBuilder.builder()
            .withGradleUserHomeDir(File(userHome, ".gradle"))
            .withProjectDir(dir)
            .build()
    }

    /**
     * Creates a Provisioner for the given repositories.
     *
     * The first time a project is created, there are ~7 seconds of configuration
     * which will go away for all subsequent runs.
     *
     * Every call to resolve will take about 1 second, even when all artifacts are resolved.
     */
    private fun createWithRepositories(repoConfig: RepositoryHandler.() -> Unit): Provisioner { // Running this takes ~3 seconds the first time it is called. Probably because of classloading.
        val tempDir = Files.createTempDir()
        val project = gradleProject(tempDir)
        project.repositories.repoConfig()
        return Provisioner { withTransitives: Boolean, mavenCoords: Collection<String?> ->
            val deps: Array<Dependency> = mavenCoords
                .map { project.dependencies.create(it as String) }
                .toTypedArray()
            val config = project.configurations.detachedConfiguration(*deps)
            config.isTransitive = withTransitives
            config.description = mavenCoords.toString()
            try {
                config.resolve()
            } catch (e: ResolveException) { /* Provide Maven coordinates in exception message instead of static string 'detachedConfiguration' */
                throw ResolveException(config.description!!, e)
            } finally { // delete the temp dir
                java.nio.file.Files.walk(tempDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { obj: Path -> obj.toFile() }
                    .forEach { obj: File -> obj.delete() }
            }
        }
    }

    /** Creates a Provisioner which will cache the result of previous calls.  */
    private fun caching(
        name: String,
        input: Supplier<Provisioner>
    ): Provisioner {
        val autostyleDir = File(StandardSystemProperty.USER_DIR.value()).parentFile
        val testlib = File(autostyleDir, "testlib")
        val cacheFile = File(testlib, "build/tmp/testprovisioner.$name.cache")
        val cached = if (cacheFile.exists()) {
            ObjectInputStream(Files.asByteSource(cacheFile).openBufferedStream()).use {
                it.readObject() as MutableMap<Set<String>, Set<File>>
            }
        } else {
            mutableMapOf()
        }
        return Provisioner { withTransitives: Boolean, mavenCoordsRaw: Collection<String> ->
            val mavenCoords = mavenCoordsRaw.toSet()
            synchronized(TestProvisioner) {
                var result = cached[mavenCoords]
                // double-check that depcache pruning hasn't removed them since our cache cached them
                val filesExist = result?.all { it.isFile && it.length() > 0 }
                if (filesExist != true) {
                    result = input.get().provisionWithTransitives(
                        withTransitives,
                        mavenCoords
                    )
                    cached[mavenCoords] = result
                    ObjectOutputStream(Files.asByteSink(cacheFile).openBufferedStream()).use {
                        it.writeObject(cached)
                    }
                }
                result!!
            }
        }
    }

    /** Creates a Provisioner for the jcenter repo.  */
    @JvmStatic
    fun jcenter(): Provisioner = jcenter

    private val jcenter by lazy {
        caching("jcenter", Supplier {
            createWithRepositories { jcenter() }
        })
    }

    /** Creates a Provisioner for the mavenCentral repo.  */
    @JvmStatic
    fun mavenCentral(): Provisioner = mavenCentral

    private val mavenCentral by lazy {
        caching("mavenCentral", Supplier {
            createWithRepositories { mavenCentral() }
        })
    }

    /** Creates a Provisioner for the local maven repo for development purpose.  */
    fun mavenLocal(): Provisioner = mavenLocal.get()

    private val mavenLocal = Supplier {
        createWithRepositories { mavenLocal() }
    }

    /** Creates a Provisioner for the Sonatype snapshots maven repo for development purpose.  */
    fun snapshots(): Provisioner = snapshots.get()

    private val snapshots = Supplier {
        createWithRepositories {
            maven {
                it.setUrl("https://oss.sonatype.org/content/repositories/snapshots")
            }
        }
    }
}
