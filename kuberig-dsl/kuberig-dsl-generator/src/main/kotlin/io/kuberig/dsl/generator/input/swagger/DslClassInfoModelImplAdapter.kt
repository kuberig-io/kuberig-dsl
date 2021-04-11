package io.kuberig.dsl.generator.input.swagger

import io.swagger.models.ModelImpl

object DslClassInfoModelImplAdapter {

    fun toDslClassInfo(model: ModelImpl): DslClassInfo {
        return DslClassInfo(
                model.type,
                model.description,
                if (model.properties == null) {
                    mapOf()
                } else {
                    model.properties.toMap()
                },
                model.format,
                model.vendorExtensions,
                if (model.required == null) {
                    listOf()
                } else {
                    model.required.toList()
                }
        )
    }
}