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

abstract class DslAttributeMeta(
    val name: String,
    val description: String,
    private val required: Boolean
) {

    fun isOptional(): Boolean {
        return !this.required
    }

    fun getterMethodName() : String {
        val splitIndex = 1

        val wrappingNeeded = name.startsWith('$')

        val methodName = "get" + this.name.substring(0, splitIndex).toUpperCase() + this.name.substring(splitIndex)

        return if (wrappingNeeded) {
            "`$methodName`"
        } else {
            methodName
        }
    }

    abstract fun absoluteAttributeDeclarationType() : DslTypeName

    abstract fun attributeDeclarationType() : String

    abstract fun toValueCall(attributeName: String) : String
}

