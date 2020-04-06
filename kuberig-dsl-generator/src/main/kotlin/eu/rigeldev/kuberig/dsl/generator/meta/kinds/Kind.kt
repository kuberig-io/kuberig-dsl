package eu.rigeldev.kuberig.dsl.generator.meta.kinds

data class Kind (val group: String, val kind: String, val version: String) {
    fun apiVersion(): String {
        return if (this.group != "") {
            "$group/$version"
        } else {
            version
        }
    }
}