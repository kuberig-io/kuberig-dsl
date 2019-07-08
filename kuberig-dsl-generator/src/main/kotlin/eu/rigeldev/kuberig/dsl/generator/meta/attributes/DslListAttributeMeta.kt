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

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslListAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return DslTypeName("List")
    }

    override fun attributeDeclarationType(): String {
        return "List<${itemType.typeShortName()}>"
    }

    override fun toValueCall(attributeName: String): String {
        return "this.$attributeName.toValue()"
    }
}