package eu.rigeldev.kuberig.dsl.generator.meta.kinds

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslKindMeta(val typeName : DslTypeName,
                  val group : String,
                  val kind : String,
                  val version : String) {

    fun kindType() : DslTypeName {
        return if (group == "") {
            DslTypeName("kinds.$version.${kind}Dsl")
        } else {
            DslTypeName("kinds.$group.$version.${kind}Dsl")
        }
    }

    /**
     * Correctly converts the name of a kind to a method name.
     *
     * Deals with kind names like WatchEvent but also APIVersion correctly (notice the difference in uppercase letters).
     */
    fun methodName() : String {
        val kindCharacters = kind.toCharArray()
        var firstLowerCaseLetterIndex : Int = -1

        val characterIterator = kindCharacters.iterator()
        var characterIndex = 0

        while(firstLowerCaseLetterIndex == -1 && characterIterator.hasNext()) {
            val currentCharacter = characterIterator.next()

            if (currentCharacter.isLowerCase()) {
                firstLowerCaseLetterIndex = characterIndex
            }

            characterIndex++
        }

        val splitIndex = if (firstLowerCaseLetterIndex == 1) {
            1
        } else {
            firstLowerCaseLetterIndex -1
        }

        return kind.substring(0, splitIndex).toLowerCase() + kind.substring(splitIndex)
    }

}
