package io.kuberig.dsl.generator.output.kotlin

import io.kuberig.dsl.generator.meta.DslTypeName
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

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
