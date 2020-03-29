package eu.rigeldev.kuberig.dsl.generator.gradle;

import org.junit.jupiter.api.Test;

public class KubernetesDslGenerationTest extends AbstractDslGenerationTest {

    public KubernetesDslGenerationTest() {
        super("kubernetes");
    }

    /**
     * We include this version specifically because this is the first version with
     * a 'x-kubernetes-xyz' attribute that is not a simple type.
     */
    @Test
    public void testKubernetesVersion1dot16dot0() throws Exception {
        this.attemptDslGenerationAndProjectCompilation("1.16.0");
    }

}
