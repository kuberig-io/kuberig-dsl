package io.kuberig.dsl.generator.output.kotlin

import io.kuberig.dsl.generator.meta.DslTypeName
import io.kuberig.dsl.generator.output.kotlin.KotlinClassWriter
import io.kuberig.dsl.generator.output.kotlin.KotlinClassWriterProducer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class KotlinClassWriterTest {

    @Test
    fun verifyKotlinSafeNames() {

        val writer = KotlinClassWriter(DslTypeName("JustTesting"), KotlinClassWriterProducer(File("/tmp")))

        // text checks
        assertEquals("_test", writer.kotlinSafe("_test"))
        assertEquals("`${'$'}test`", writer.kotlinSafe("${'$'}test"))
        assertEquals("extKubernetesAttr", writer.kotlinSafe("ext-kubernetes-attr"))
        assertEquals("metadata", writer.kotlinSafe("metadata"))
        assertEquals("some2", writer.kotlinSafe("some2"))

        // hard keyword checks
        assertEquals("`object`", writer.kotlinSafe("object"))
        assertEquals("`null`", writer.kotlinSafe("null"))

        // prevent double wrapping
        assertEquals("`x-kubernetes-int-or-string`", writer.kotlinSafe("`x-kubernetes-int-or-string`"))
    }

}
