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

package eu.rigeldev.kuberig.dsl.processing

import eu.rigeldev.kuberig.dsl.DslType
import java.lang.IllegalStateException
import javax.naming.Context

object DslProcessingContext {
    private val dslTypePreProcessors = ThreadLocal<MutableList<DslTypePreProcessor>>()
    private val dslTypePostProcessors = ThreadLocal<MutableList<DslTypePostProcessor>>()
    private val dslTypeStack = ThreadLocal<MutableList<Any>>()

    private val actionEntriesTL = ThreadLocal<MutableList<ContextActionEntry>>()

    fun init() {
        this.dslTypePreProcessors.set(mutableListOf())
        this.dslTypePostProcessors.set(mutableListOf())
        this.dslTypeStack.set(mutableListOf())
        this.actionEntriesTL.set(mutableListOf())
    }

    fun process() {
        val actionEntries = this.actionEntriesTL.get()


        val actionEntriesIterator = actionEntries.iterator()

        var level = 0

        while(actionEntriesIterator.hasNext()) {

            val currentActionEntry = actionEntriesIterator.next()

            if (level == 0 && currentActionEntry.action == ContextAction.POP) {
                throw IllegalStateException("At level 0 but encountered POP for " + currentActionEntry.dslType::class.java.name)
            }

            when(currentActionEntry.action) {
                ContextAction.PUSH -> {
                    level++

                    println("${currentActionEntry.action} >[$level] " + currentActionEntry.dslType::class.java.name)

                }
                ContextAction.POP -> {
                    level--

                    println("${currentActionEntry.action} [$level]< " + currentActionEntry.dslType::class.java.name)
                }
            }
        }


    }

    fun registerDslTypePreProcessor(preProcessor: DslTypePreProcessor) {
        this.dslTypePreProcessors.get().add(preProcessor)
    }

    fun registerDslTypePostProcessor(postProcess: DslTypePostProcessor) {
        this.dslTypePostProcessors.get().add(postProcess)
    }

    fun push(dslType: DslType<Any>) {
        this.actionEntriesTL.get().add(ContextActionEntry(ContextAction.PUSH, dslType))
    }

    fun pop(dslType: DslType<Any>) {
        this.actionEntriesTL.get().add(ContextActionEntry(ContextAction.POP, dslType))
    }

    fun dslTypeStack() : List<Any> {
        return this.dslTypeStack.get().toList()
    }
}

enum class ContextAction {
    PUSH,
    POP
}

data class ContextActionEntry(val action: ContextAction, val dslType: DslType<Any>)