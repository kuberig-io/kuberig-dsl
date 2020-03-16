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

package eu.rigeldev.kuberig.dsl.generator.meta

class DslTypeName(rawAbsoluteName : String) {

    // compensate for apiextensions-apiserver in package name
    val absoluteName : String = rawAbsoluteName.replace('-', '.')

    private fun dottedName() : Boolean {
        return absoluteName.contains('.')
    }

    fun requiresImport() : Boolean {
        return this.dottedName()
    }

    fun isNotPlatformType(): Boolean {
        return !absoluteName.startsWith("java")
    }

    fun packageName() : String {
        return if (this.requiresImport()) {
            val splits = absoluteName.split(".")
            splits.subList(0, splits.size - 1).joinToString(".")
        } else {
            ""
        }
    }

    fun typeShortName(): String {
        return if (this.dottedName()) {
            absoluteName.split('.').last()
        } else {
            this.absoluteName
        }
    }

    fun methodName() : String {
        val typeShortName = this.typeShortName()

        return typeShortName.substring(0, 1).toLowerCase() + typeShortName.substring(1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DslTypeName

        if (absoluteName != other.absoluteName) return false

        return true
    }

    override fun hashCode(): Int {
        return absoluteName.hashCode()
    }


}
