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

package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * API Types with a format like 'int-or-string'.
 *
 * These are modelled as sealed kotlin classes.
 *
 * A custom Jackson Serializer is generated for these types. To preserve correct YAML/JSON output.
 */
class DslSealedTypeMeta(
    typeName: DslTypeName,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val sealedTypes: Map<String, DslTypeName>
) : DslTypeMeta(typeName, description, typeDependencies)