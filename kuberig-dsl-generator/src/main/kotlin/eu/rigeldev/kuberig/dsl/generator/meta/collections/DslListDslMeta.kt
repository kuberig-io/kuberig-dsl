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

package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta

class DslListDslMeta(type : DslTypeName,
                     meta : DslListAttributeMeta,
                     plural : Boolean
) :DslCollectionDslMeta<DslListAttributeMeta>(type, meta, plural) {

    override fun itemType(): DslTypeName {
        return this.meta.itemType
    }

    override fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName
                + meta.name.substring(0, 1).toUpperCase()
                + meta.name.substring(1)
                + "Dsl"
        )
    }

}