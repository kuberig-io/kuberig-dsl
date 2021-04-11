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

package io.kuberig.dsl.generator.meta.collections

import io.kuberig.dsl.generator.meta.DslTypeName
import io.kuberig.dsl.generator.meta.attributes.DslAttributeMeta

abstract class DslCollectionDslMeta<AM : DslAttributeMeta>(val type : DslTypeName,
                                                           val meta : AM,
                                                           val plural : Boolean) {

    abstract fun declarationType() : DslTypeName

    abstract fun itemType() : DslTypeName

    fun complexItemType() : Boolean {
        return this.itemType().requiresImport() && this.itemType().isNotPlatformType()
    }

    private fun itemTypeSuffix() : String {
        return if (this.complexItemType()) {
            "Dsl"
        } else {
            ""
        }
    }

    fun dslItemType() : DslTypeName {
        return DslTypeName(this.itemType().absoluteName + this.itemTypeSuffix())
    }

    /**
     * Check if attribute name this list is for is plural
     * most likely this does not cover all non-plural cases (to-be-verified).
     */
    fun addMethodName() : String {
        return if (this.plural) {
            return if (this.meta.name.endsWith("ies")) {
                meta.name.substring(0, meta.name.length - 3) + "y"
            } else {
                meta.name.substring(0, meta.name.length - 1)
            }
        } else {
            "item"
        }
    }
}