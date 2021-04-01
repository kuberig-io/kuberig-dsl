package io.kuberig.dsl.generator.input.swagger

import io.swagger.models.properties.Property

data class DslClassInfo(
        val type: String?,
        val description: String?,
        val properties: Map<String, Property>,
        val format: String?,
        val vendorExtensions: Map<String, Any>,
        val required: List<String>
)