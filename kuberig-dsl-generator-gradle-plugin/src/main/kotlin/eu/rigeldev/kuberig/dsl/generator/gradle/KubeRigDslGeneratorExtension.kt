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

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class KubeRigDslGeneratorExtension(project : Project) {
    /**
     * By convention the Swagger API specification file is expected in the 'src/main/resources' directory.
     * And the filename needs to be 'swagger.json'.
     */
    val swaggerFileLocation : Property<String> = project.objects.property(String::class.java)
    /**
     * By convention the DSL sources are generated in
     */
    val sourceOutputDirectoryLocation : Property<String> = project.objects.property(String::class.java)

    init {
        this.swaggerFileLocation.set("src/main/resources/swagger.json")
        this.sourceOutputDirectoryLocation.set("build/generated-src/main/kotlin")
    }
}