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

package eu.rigeldev.kuberig.dsl.generator.meta.kinds

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslKindMeta(val typeName : DslTypeName,
                  val group : String,
                  val kind : String,
                  val version : String) {

    fun kindType() : DslTypeName {
        return if (group == "") {
            DslTypeName("kinds.$version.${kind}Dsl")
        } else {
            DslTypeName("kinds.$group.$version.${kind}Dsl")
        }
    }

    /**
     * Correctly converts the name of a kind to a method name.
     *
     * Deals with kind names like WatchEvent but also APIVersion correctly (notice the difference in uppercase letters).
     */
    fun methodName() : String {
        val kindCharacters = kind.toCharArray()
        var firstLowerCaseLetterIndex : Int = -1

        val characterIterator = kindCharacters.iterator()
        var characterIndex = 0

        while(firstLowerCaseLetterIndex == -1 && characterIterator.hasNext()) {
            val currentCharacter = characterIterator.next()

            if (currentCharacter.isLowerCase()) {
                firstLowerCaseLetterIndex = characterIndex
            }

            characterIndex++
        }

        val splitIndex = if (firstLowerCaseLetterIndex == 1) {
            1
        } else {
            firstLowerCaseLetterIndex -1
        }

        return kind.substring(0, splitIndex).toLowerCase() + kind.substring(splitIndex)
    }

    fun apiVersion() : String {
        return if (this.group != "") {
            "$group/$version"
        } else {
            version
        }
    }

}
