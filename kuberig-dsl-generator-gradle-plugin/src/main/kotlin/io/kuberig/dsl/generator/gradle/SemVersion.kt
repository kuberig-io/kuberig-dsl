package io.kuberig.dsl.generator.gradle

import java.lang.IllegalStateException

class SemVersion(val versionText: String, val majorVersion: Int, val minorVersion: Int, val patchVersion: Int) {
    companion object {
        fun fromVersionText(versionText: String): SemVersion {
            val versionTextOnly = if (versionText.contains('-')) {
                versionText.subSequence(0, versionText.indexOf('-'))
            } else {
                versionText
            }
            val parts = versionTextOnly.split('.')

            check(parts.size == 3) { "$versionText is not a sem-version, does not have 3 parts separated by periods."}

            try {
                return SemVersion(
                    versionText,
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
                )
            }
            catch (e: NumberFormatException) {
                throw IllegalStateException("Not all parts in $versionText are numbers", e)
            }
        }
    }
}