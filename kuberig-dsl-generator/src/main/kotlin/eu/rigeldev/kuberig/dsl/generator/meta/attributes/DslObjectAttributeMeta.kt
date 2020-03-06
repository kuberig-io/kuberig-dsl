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

class DslObjectAttributeMeta(
    name: String,
    description: String,
    required: Boolean,
    val absoluteType: DslTypeName
) : DslAttributeMeta(name, description, required) {
    override fun attributeDeclarationType(): String {
        return this.absoluteType.typeShortName()
    }

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return this.absoluteType
    }

    override fun toValueConstructorSuffix(): String {
        return if (this.absoluteType.requiresImport() && this.absoluteType.isNotPlatformType()) {
            return if (super.isOptional()) {
                "?.toValue()"
            } else {
                "!!.toValue()"
            }
        } else {
            ""
        }
    }
}