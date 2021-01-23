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

package io.kuberig.dsl.generator

import io.kuberig.dsl.generator.input.DslMetaProducer
import io.kuberig.dsl.generator.input.swagger.SwaggerDslMetaProducer
import io.kuberig.dsl.generator.output.DslMetaConsumer
import io.kuberig.dsl.generator.output.kotlin.KotlinDslMetaConsumer
import java.io.File


class DslCodeGenerator(projectRootDirectory: File) {
    /**
     * By convention the Swagger API specification file is expected in the 'src/main/resources' directory.
     * And the filename needs to be 'swagger.json'.
     */
    private val swaggerFile = File(projectRootDirectory, "src/main/resources/swagger.json")
    /**
     *
     */
    private val sourceOutputDirectory = File(projectRootDirectory, "build/generated-src/main/kotlin")

    fun generate() {
        // input for dsl meta
        val dslMetaProducer : DslMetaProducer = SwaggerDslMetaProducer(swaggerFile)

        val dslMeta = dslMetaProducer.provide()

        // output from dsl meta

        val kotlinDslMetaConsumer : DslMetaConsumer = KotlinDslMetaConsumer(sourceOutputDirectory)

        kotlinDslMetaConsumer.consume(dslMeta)
    }
}