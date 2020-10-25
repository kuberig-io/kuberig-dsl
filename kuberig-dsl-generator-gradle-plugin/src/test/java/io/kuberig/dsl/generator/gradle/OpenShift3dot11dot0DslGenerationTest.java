package io.kuberig.dsl.generator.gradle;

import org.junit.jupiter.api.Test;

public class OpenShift3dot11dot0DslGenerationTest extends AbstractDslGenerationTest {

    public OpenShift3dot11dot0DslGenerationTest() {
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

}
