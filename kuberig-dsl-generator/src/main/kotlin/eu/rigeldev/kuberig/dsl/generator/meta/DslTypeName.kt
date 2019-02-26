package eu.rigeldev.kuberig.dsl.generator.meta

class DslTypeName(val absoluteName : String) {

    private fun dottedName() : Boolean {
        return absoluteName.contains('.')
    }

    fun requiresImport() : Boolean {
        return this.dottedName()
    }

    fun packageName() : String {
        return if (this.requiresImport()) {
            val splits = absoluteName.split(".")
            splits.subList(0, splits.size - 1).joinToString(".")
        } else {
            ""
        }
    }

    fun typeShortName(): String {
        return if (this.dottedName()) {
            absoluteName.split('.').last()
        } else {
            this.absoluteName
        }
    }

    fun methodName() : String {
        val typeShortName = this.typeShortName()

        return typeShortName.substring(0, 1).toLowerCase() + typeShortName.substring(1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DslTypeName

        if (absoluteName != other.absoluteName) return false

        return true
    }

    override fun hashCode(): Int {
        return absoluteName.hashCode()
    }


}
