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

class DslKindContainer(val name : String) {
    val subContainers = mutableMapOf<String, DslKindContainer>()
    val kinds = mutableMapOf<String, DslKindMeta>()

    fun add(dslKindMeta: DslKindMeta) {

        val containerNameParts = mutableListOf<String>()
        if (dslKindMeta.group != "") {
            val groupSplits = dslKindMeta.group.split('.')
            containerNameParts.addAll(groupSplits)
        }
        containerNameParts.add(dslKindMeta.version)

        this.add(containerNameParts.iterator(), dslKindMeta)
    }

    private fun add(containerNamePartIterator : Iterator<String>, dslKindMeta : DslKindMeta) {
        if (containerNamePartIterator.hasNext()) {
            val group = containerNamePartIterator.next()

            val groupContainer : DslKindContainer

            if (subContainers.containsKey(group)) {
                groupContainer = subContainers[group]!!
            } else {
                groupContainer = DslKindContainer(group)
                subContainers[group] = groupContainer
            }

            groupContainer.add(containerNamePartIterator, dslKindMeta)
        }
        else {
            kinds[dslKindMeta.kind] = dslKindMeta
        }
    }

    fun typeName() : String {
        return "Dsl${name.capitalize()}Root"
    }

}