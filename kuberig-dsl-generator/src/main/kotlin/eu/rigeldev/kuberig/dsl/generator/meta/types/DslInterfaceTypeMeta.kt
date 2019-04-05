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
 * API Types without attributes.
 *
 * I guess these are special types that will need manual handling (work-in-progress).
 * I need to understand these better.
 */
class DslInterfaceTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)