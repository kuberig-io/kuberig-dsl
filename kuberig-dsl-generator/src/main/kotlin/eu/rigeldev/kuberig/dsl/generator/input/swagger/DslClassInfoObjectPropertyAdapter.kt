package eu.rigeldev.kuberig.dsl.generator.input.swagger

import io.swagger.models.properties.ObjectProperty

object DslClassInfoObjectPropertyAdapter {

    fun toDslClassInfo(objectProperty: ObjectProperty): DslClassInfo {
        return DslClassInfo(
                "object",
                objectProperty.description,
                if (objectProperty.properties == null) {
                    mapOf()
                } else {
                    objectProperty.properties.toMap()
                },
                objectProperty.format,
                objectProperty.vendorExtensions,
                if (objectProperty.requiredProperties == null) {
                    listOf()
                } else {
                    objectProperty.requiredProperties.toList()
                }
        )
    }

}