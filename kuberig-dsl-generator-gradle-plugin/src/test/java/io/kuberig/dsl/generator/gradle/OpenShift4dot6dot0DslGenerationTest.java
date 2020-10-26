package io.kuberig.dsl.generator.gradle;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Not able to get this working on circleci, haven't pin pointed the real problem.")
public class OpenShift4dot6dot0DslGenerationTest extends AbstractDslGenerationTest {

    public OpenShift4dot6dot0DslGenerationTest() {
        super("openshift");
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
