package kinds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import eu.rigeldev.kuberig.dsl.DslResource
import eu.rigeldev.kuberig.dsl.DslResourceSink

class YamlOutputSink : DslResourceSink {

    private val objectMapper : ObjectMapper

    init {
        val yamlFactory = YAMLFactory()
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

        this.objectMapper = ObjectMapper(yamlFactory)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    override fun <T> add(resource: DslResource<T>) {
        println("#---alias:${resource.alias}---")
        println(this.objectMapper.writeValueAsString(resource.dslType.toValue()))
    }
}