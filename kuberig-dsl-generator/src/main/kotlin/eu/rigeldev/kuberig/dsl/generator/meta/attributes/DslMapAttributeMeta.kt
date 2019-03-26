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

package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslMapAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    private val keyType : DslTypeName,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {
    var mapDslMeta : DslMapDslMeta? = null

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return DslTypeName("Map")
    }

    override fun attributeDeclarationType(): String {
        return "Map<${keyType.typeShortName()}, ${itemType.typeShortName()}>"
    }

    override fun toValueConstructorSuffix(): String {
        return ".toValue()"
    }
}