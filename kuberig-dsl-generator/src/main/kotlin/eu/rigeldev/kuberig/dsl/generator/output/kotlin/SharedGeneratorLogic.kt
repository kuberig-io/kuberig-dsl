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

package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

fun fileGeneratorFunction(classWriter: KotlinClassWriter, dslTypeName: DslTypeName, declarationType: DslTypeName) {
    val fileMethodCode = mutableListOf<String>()
    fileMethodCode.add("val gen = ${dslTypeName.typeShortName()}()")
    fileMethodCode.add("@Suppress(\"UNCHECKED_CAST\")")
    fileMethodCode.add("DslProcessingContext.push(gen as DslType<Any>)")
    fileMethodCode.add("gen.init()")
    fileMethodCode.add("@Suppress(\"UNCHECKED_CAST\")")
    fileMethodCode.add("DslProcessingContext.pop(gen as DslType<Any>)")
    fileMethodCode.add("return gen")

    classWriter.fileMethod(
        methodName = declarationType.methodName(),
        methodParameters = listOf(
            Pair("init", "${dslTypeName.typeShortName()}.() -> Unit")
        ),
        methodReturnType = dslTypeName.typeShortName(),
        methodCode = fileMethodCode,
        methodTypeDependencies = listOf(
            "eu.rigeldev.kuberig.dsl.processing.DslProcessingContext"
        )
    )
}

fun complexTypeInitMethod(classWriter: KotlinClassWriter,
                          methodName:String,
                          variableName:String,
                          variableTypeName:String,
                          declarationNeeded : Boolean = false,
                          useStatement:String = "",
                          extraParameter: Pair<String, String>? = null,
                          methodDocumentation: String = "") {
    val methodParameters = mutableListOf<Pair<String, String>>()
    if (extraParameter != null) {
        methodParameters.add(extraParameter)
    }
    methodParameters.add(Pair("init", "$variableTypeName.() -> Unit"))

    val methodCode = mutableListOf<String>()
    if (declarationNeeded) {
        methodCode.add("val $variableName = $variableTypeName()")
    }
    methodCode.add("@Suppress(\"UNCHECKED_CAST\")")
    methodCode.add("DslProcessingContext.push($variableName as DslType<Any>)")
    methodCode.add("$variableName.init()")
    methodCode.add("@Suppress(\"UNCHECKED_CAST\")")
    methodCode.add("DslProcessingContext.pop($variableName as DslType<Any>)")
    if (useStatement != "") {
        methodCode.add(useStatement)
    }

    classWriter.typeMethod(
        methodDocumentation = methodDocumentation,
        methodName = methodName,
        methodParameters = methodParameters,
        methodCode = methodCode,
        methodTypeDependencies = listOf(
            "eu.rigeldev.kuberig.dsl.processing.DslProcessingContext"
        )
    )
}