package io.kuberig.dsl.generator.gradle;

import org.junit.jupiter.api.Test;

public class OpenShiftDslGenerationTest extends AbstractDslGenerationTest {

    public OpenShiftDslGenerationTest() {
        super("openshift");
    }

    /**
     * From OpenShift 3.7.0 onwards the OpenShift api specifications also contain the kind meta data in all lower
     * case like in the Kubernetes api specifications.
     */
    @Test
    public void testOpenShiftVersion3dot11dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("3.11.0");
    }

    /**
     * OpenShift 4.6.0 broke the DSL generation in 5 different ways:
     * - It had the first all upper case named kind: 'DNS'
     * - It had a case when the current package split on '-' cause the keyword 'package' to be used as a package name.
     * - It had 3 Kinds that did not have the kind and apiVersion attributes.
     * - It had a new 'semver' format for the string type.
     * - It had the first use of a DecimalProperty type.
     */
    @Test
    public void testOpenShiftVersion4dot6dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("4.6.0");
    }
}
