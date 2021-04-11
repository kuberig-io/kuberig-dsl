package io.kuberig.dsl.generator.gradle

import kotlin.test.Test

class Openshift3_11_0 : FunctionalTest() {

    /**
     * From OpenShift 3.7.0 onwards the OpenShift api specifications also contain the kind meta data in all lower
     * case like in the Kubernetes api specifications.
     */
    @Test fun openshiftVersion3_11_0() {
        testFor("openshift", "3.11.0")
    }
}