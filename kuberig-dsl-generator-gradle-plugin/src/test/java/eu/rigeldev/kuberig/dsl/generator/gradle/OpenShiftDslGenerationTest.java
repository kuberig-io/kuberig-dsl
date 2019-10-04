package eu.rigeldev.kuberig.dsl.generator.gradle;

import org.junit.jupiter.api.Test;

public class OpenShiftDslGenerationTest extends AbstractDslGenerationTest {

    public OpenShiftDslGenerationTest() {
        super("openshift");
    }

    /**
     * We include this OpenShift version specifically because the kind meta data in this version was specified with
     * camel case Group, Kind, Version instead of all lower case like in the Kubernetes api specifications.
     */
    @Test
    public void testOpenShiftVersion3dot6dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("3.6.0");
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
