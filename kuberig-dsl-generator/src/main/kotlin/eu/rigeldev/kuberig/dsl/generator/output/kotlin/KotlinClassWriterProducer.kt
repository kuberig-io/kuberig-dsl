package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class KotlinClassWriterProducer(private val sourceOutputDirectory : File) {

    /**
     * @param typeName The absolute type name (including package name).
     */
    fun classWriter(typeName : String) : BufferedWriter {
        val allSplits = typeName.split(".", "-")
        val directorySplits = allSplits.subList(0, allSplits.size - 1)

        var currentDirectory = sourceOutputDirectory
        if (!currentDirectory.exists()) {
            currentDirectory.mkdirs()
        }
        directorySplits.forEach {
            currentDirectory = File(currentDirectory, it)
            if (!currentDirectory.exists() && !currentDirectory.mkdir()) {
                throw IllegalStateException("Failed to create ${currentDirectory.absolutePath}")
            }
        }

        return BufferedWriter(OutputStreamWriter(FileOutputStream(
            File(currentDirectory, allSplits.last() + ".kt")),
            StandardCharsets.UTF_8)
        )
    }

}