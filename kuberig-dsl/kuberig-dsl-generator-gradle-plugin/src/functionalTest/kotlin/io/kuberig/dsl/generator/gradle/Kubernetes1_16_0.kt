package io.kuberig.dsl.generator.gradle

import kotlin.test.Test

class Kubernetes1_16_0 : FunctionalTest() {

    /**
     * We include this version specifically because this is the first version with
     * a 'x-kubernetes-xyz' attribute that is not a simple type.
     */
    @Test fun kubernetesVersion1_16_0() {
        testFor("kubernetes", "1.16.0")
    }

}