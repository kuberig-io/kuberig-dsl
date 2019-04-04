/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.rigeldev.kuberig.dsl.generator.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

open class KubeRigDslGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")
        project.plugins.apply("idea")
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        val extension = project.extensions.create("kuberigDsl", KubeRigDslGeneratorExtension::class.java, project)

        val props = this.loadProps()
        val kubeRigVersion = props["kuberig.version"]
        val jacksonVersion = props["jackson.version"]

        project.dependencies.add(
            "implementation",
            "com.fasterxml.jackson.core:jackson-core:$jacksonVersion"
        )
        project.dependencies.add(
            "implementation",
            "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion"
        )
        project.dependencies.add(
            "implementation",
            "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion"
        )
        project.dependencies.add(
            "implementation",
            "com.fasterxml.jackson.module:jackson-modules-java8:$jacksonVersion"
        )
        project.dependencies.add(
            "implementation",
            "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion"
        )
        project.dependencies.add(
            "implementation",
            "eu.rigeldev.kuberig:kuberig-dsl-base:$kubeRigVersion"
        )

        project.tasks.register("generateDslSource", KubeRigDslGeneratorTask::class.java) { generateDslSourceTask ->
            generateDslSourceTask.getSwaggerFile().set(project.file(extension.swaggerFileLocation))
            generateDslSourceTask.getSourceOutputDirectory().set(project.file(extension.sourceOutputDirectoryLocation))
        }

        project.tasks.withType(KotlinCompile::class.java) {
            it.dependsOn("generateDslSource")
        }

        project.afterEvaluate {
            val sourceOutputDirectory = it.file(extension.sourceOutputDirectoryLocation)

            val kotlin = it.extensions.getByType(KotlinProjectExtension::class.java)

            kotlin.sourceSets.getByName("main").apply {
                this.kotlin.srcDir(sourceOutputDirectory)
            }

            val idea = it.extensions.getByType(IdeaModel::class.java)

            idea.module.apply {
                generatedSourceDirs.add(sourceOutputDirectory)
            }
        }
    }

    private fun loadProps() : Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
    }
}