package io.kuberig.dsl.generator.gradle

import kotlin.test.Test

class Openshift4_6_0 : FunctionalTest() {
    /**
     * OpenShift 4.6.0 broke the DSL generation in 5 different ways:
     * - It had the first all upper case named kind: 'DNS'
     * - It had a case when the current package split on '-' cause the keyword 'package' to be used as a package name.
     * - It had 3 Kinds that did not have the kind and apiVersion attributes.
     * - It had a new 'semver' format for the string type.
     * - It had the first use of a DecimalProperty type.
     */
    @Test fun openshiftVersion4_6_0() {
        testFor("openshift", "4.6.0")
    }
}